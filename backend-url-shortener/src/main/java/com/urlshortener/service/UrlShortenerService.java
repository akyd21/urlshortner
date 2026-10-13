package com.urlshortener.service;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, String userEmail) {
        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        String shortCode;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            shortCode = shortCodeGenerator.generateCode();
            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException("Failed to generate a unique short code after " + maxAttempts + " attempts.");
            }
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());

        ShortUrl newUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .userEmail(userEmail)
                .build();

        return shortUrlRepository.save(newUrl);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> getOriginalUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }
}
