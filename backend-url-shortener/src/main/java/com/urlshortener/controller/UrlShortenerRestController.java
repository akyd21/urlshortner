package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlShortenerRestController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody ShortenUrlRequest request,
                                        HttpServletRequest httpRequest) {

        String userEmail = null;
        if (httpRequest.getUserPrincipal() instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            userEmail = token.getPrincipal().getAttribute("email");
        }
        
        ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getUrl(), userEmail);
        
        // Construct the full short URL dynamically based on the current request
        // Since the backend is running on a port and the frontend is running on another,
        // we might want to return the base URL of the frontend. But for simplicity,
        // we will just return the short code and let the frontend construct it, or
        // we can return the backend URL and the frontend can redirect through it.
        // The current backend redirect logic is in HomeController mapped to `/{shortCode}`.
        // So the redirect URL is the backend's base URL + shortCode.
        
        String appUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        String fullShortUrl = appUrl + "/" + shortUrl.getShortCode();
        
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", fullShortUrl);
        response.put("originalUrl", shortUrl.getOriginalUrl());
        response.put("shortCode", shortUrl.getShortCode());
        
        return ResponseEntity.ok(response);
    }
}
