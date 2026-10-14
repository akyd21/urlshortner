package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlShortenerRestController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody ShortenUrlRequest request,
                                        HttpServletRequest httpRequest,
                                        Principal principal) {

        ShortUrl shortUrl = urlShortenerService.createShortUrl(
                request.getUrl(), request.getAlias(), request.getExpiresAt(), principal.getName());

        String appUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        String fullShortUrl = appUrl + "/" + publicCode(shortUrl);
        
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", fullShortUrl);
        response.put("originalUrl", shortUrl.getMapping().getOriginalUrl());
        response.put("shortCode", publicCode(shortUrl));
        response.put("clickCount", String.valueOf(clickCount(shortUrl)));
        response.put("expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString());
        response.put("status", shortUrl.getStatus());
        response.put("alias", alias(shortUrl));
        response.put("domain", shortUrl.getMapping().getDomain());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shorten/bulk")
    public List<Map<String, String>> shortenBulk(@Size(max = 50, message = "You can shorten at most 50 URLs at once.")
                                                  @Valid @RequestBody List<@Valid ShortenUrlRequest> requests,
                                                  HttpServletRequest httpRequest,
                                                  Principal principal) {
        String baseUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        return requests.stream().map(request -> {
            ShortUrl shortUrl = urlShortenerService.createShortUrl(
                    request.getUrl(), request.getAlias(), request.getExpiresAt(), principal.getName());
            return Map.of(
                    "originalUrl", shortUrl.getMapping().getOriginalUrl(),
                    "shortUrl", baseUrl + "/" + publicCode(shortUrl),
                    "shortCode", publicCode(shortUrl),
                    "status", shortUrl.getStatus(),
                    "alias", alias(shortUrl),
                    "domain", shortUrl.getMapping().getDomain());
        }).collect(Collectors.toList());
    }

    @GetMapping("/links/{shortCode}/stats")
    public Map<String, String> stats(@PathVariable String shortCode, Principal principal) {
        ShortUrl shortUrl = urlShortenerService.findOwnedByCode(shortCode, principal.getName());
        return Map.of(
                "shortCode", publicCode(shortUrl),
                "originalUrl", shortUrl.getMapping().getOriginalUrl(),
                "clickCount", String.valueOf(clickCount(shortUrl)),
                "createdAt", shortUrl.getCreatedAt().toString(),
                "expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString(),
                "status", shortUrl.getStatus(),
                "alias", alias(shortUrl),
                "domain", shortUrl.getMapping().getDomain());
    }

    @GetMapping("/links")
    public List<Map<String, String>> links(HttpServletRequest httpRequest, Principal principal) {
        String baseUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        return urlShortenerService.findOwnedLinks(principal.getName()).stream()
                .map(shortUrl -> {
                    Map<String, String> link = new HashMap<>();
                    link.put("shortCode", publicCode(shortUrl));
                    link.put("shortUrl", baseUrl + "/" + publicCode(shortUrl));
                    link.put("originalUrl", shortUrl.getMapping().getOriginalUrl());
                    link.put("clickCount", String.valueOf(clickCount(shortUrl)));
                    link.put("createdAt", shortUrl.getCreatedAt().toString());
                    link.put("expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString());
                    link.put("lastClickedAt", lastClickedAt(shortUrl));
                    link.put("status", shortUrl.getStatus());
                    link.put("alias", alias(shortUrl));
                    link.put("domain", shortUrl.getMapping().getDomain());
                    return link;
                })
                .toList();
    }

    @DeleteMapping("/links/{shortCode}")
    public ResponseEntity<Void> deleteLink(@PathVariable String shortCode, Principal principal) {
        urlShortenerService.deleteOwnedLink(shortCode, principal.getName());
        return ResponseEntity.noContent().build();
    }

    private String publicCode(ShortUrl shortUrl) {
        return shortUrl.getCustomAlias() == null
        ? shortUrl.getShortCode()
        : shortUrl.getCustomAlias().getAliasText();
    }

    private String alias(ShortUrl shortUrl) {
        return shortUrl.getCustomAlias() == null
                ? ""
                : shortUrl.getCustomAlias().getAliasText();
    }

    private int clickCount(ShortUrl shortUrl) {
        return shortUrl.getClickEvents().size();
    }

    private String lastClickedAt(ShortUrl shortUrl) {
        return shortUrl.getClickEvents().stream()
                .map(click -> click.getTimestamp())
                .max(java.util.Comparator.naturalOrder())
                .map(Object::toString)
                .orElse("");
    }
}
