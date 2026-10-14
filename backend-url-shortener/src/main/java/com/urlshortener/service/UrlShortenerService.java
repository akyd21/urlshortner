package com.urlshortener.service;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.CustomAliasRepository;
import com.urlshortener.entity.ClickEvent;
import com.urlshortener.entity.CustomAlias;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UserRepository userRepository;
    private final CustomAliasRepository customAliasRepository;

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, String alias, LocalDateTime expiresAt, String email) {
        User owner = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));
        List<ShortUrl> existingUrls = shortUrlRepository.findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(email);
        Optional<ShortUrl> existingUrl = existingUrls.stream()
                .filter(link -> link.getMapping() != null
                        && link.getMapping().getOriginalUrl().equals(originalUrl))
                .findFirst();
        if (existingUrl.isPresent() && (alias == null || alias.isBlank())) {
            return existingUrl.get();
        }

        String shortCode;
        if (alias != null && !alias.isBlank()) {
            if (customAliasRepository.existsByAliasTextIgnoreCase(alias)
                    || shortUrlRepository.existsByShortCode(alias)) {
                throw new IllegalArgumentException("That alias is already in use.");
            }
            shortCode = shortCodeGenerator.generateCode();
        } else {
            int attempts = 0;
            do {
                shortCode = shortCodeGenerator.generateCode();
                attempts++;
                if (attempts > 10) {
                    throw new IllegalStateException("Failed to generate a unique short code.");
                }
            } while (shortUrlRepository.existsByShortCode(shortCode));
        }

        ShortUrl newUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .expiresAt(expiresAt != null ? expiresAt : LocalDateTime.now().plusMonths(3))
                .owner(owner)
                .build();

        String domain = originalUrl;
        try {
            domain = java.net.URI.create(originalUrl).getHost();
        } catch (IllegalArgumentException ignored) {
            // The request validator has already checked the URL format.
        }
        UrlMapping mapping = new UrlMapping(newUrl, originalUrl, domain);
        newUrl.setMapping(mapping);
        if (alias != null && !alias.isBlank()) {
            CustomAlias customAlias = new CustomAlias(newUrl, alias);
            newUrl.setCustomAlias(customAlias);
        }
        return shortUrlRepository.save(newUrl);
    }

    @Transactional
    public Optional<ShortUrl> getOriginalUrl(String shortCode, String device, String location) {
        Optional<ShortUrl> result = shortUrlRepository.findByShortCode(shortCode);
        if (result.isEmpty()) {
            result = customAliasRepository.findByAliasTextIgnoreCaseAndActiveTrue(shortCode)
                    .map(CustomAlias::getShortUrl);
        }
        if (result.isEmpty()) {
            return result;
        }
        ShortUrl shortUrl = result.get();
        if (!"ACTIVE".equalsIgnoreCase(shortUrl.getStatus())
                || shortUrl.getMapping() == null
                || !shortUrl.getMapping().isActive()
                || (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now()))) {
            return Optional.empty();
        }
        shortUrl.getClickEvents().add(new ClickEvent(shortUrl, device, location));
        shortUrlRepository.save(shortUrl);
        return Optional.of(shortUrl);
    }

    public Optional<ShortUrl> getOriginalUrl(String shortCode) {
        return getOriginalUrl(shortCode, null, null);
    }

    public ShortUrl findOwnedByCode(String shortCode, String email) {
        ShortUrl shortUrl = findByCode(shortCode);
        if (shortUrl.getOwner() == null || !shortUrl.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("You do not have access to this link.");
        }
        return shortUrl;
    }

    public ShortUrl findByCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .or(() -> customAliasRepository.findByAliasTextIgnoreCaseAndActiveTrue(shortCode)
                        .map(CustomAlias::getShortUrl))
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found."));
    }

    @Transactional
    public void deleteOwnedLink(String shortCode, String email) {
        ShortUrl shortUrl = findOwnedByCode(shortCode, email);
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> findOwnedLinks(String email) {
        return shortUrlRepository.findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }
}
