package com.urlshortener.service;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UserRepository;
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

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, String alias, LocalDateTime expiresAt, String email) {
        User owner = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found."));
        List<ShortUrl> existingUrls = shortUrlRepository.findByOriginalUrl(originalUrl);
        Optional<ShortUrl> existingUrl = existingUrls.stream()
                .filter(link -> link.getOwner() != null && link.getOwner().getId().equals(owner.getId()))
                .findFirst();
        if (existingUrl.isPresent() && (alias == null || alias.isBlank())) {
            return existingUrl.get();
        }

        String shortCode;
        if (alias != null && !alias.isBlank()) {
            shortCode = alias;
            if (shortUrlRepository.existsByShortCode(shortCode)) {
                throw new IllegalArgumentException("That alias is already in use.");
            }
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
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .expiresAt(expiresAt)
                .owner(owner)
                .build();

        return shortUrlRepository.save(newUrl);
    }

    @Transactional
    public Optional<ShortUrl> getOriginalUrl(String shortCode) {
        Optional<ShortUrl> result = shortUrlRepository.findByShortCode(shortCode);
        if (result.isEmpty()) {
            return result;
        }
        ShortUrl shortUrl = result.get();
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrl.setLastClickedAt(LocalDateTime.now());
        shortUrlRepository.save(shortUrl);
        return Optional.of(shortUrl);
    }

    public ShortUrl findOwnedByCode(String shortCode, String email) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found."));
        if (shortUrl.getOwner() == null || !shortUrl.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("You do not have access to this link.");
        }
        return shortUrl;
    }

    public ShortUrl findByCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found."));
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> findOwnedLinks(String email) {
        return shortUrlRepository.findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }
}
