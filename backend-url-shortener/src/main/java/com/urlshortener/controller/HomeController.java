package com.urlshortener.controller;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;

    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginal(@PathVariable String shortCode, HttpServletRequest request) {
        String device = request.getHeader("User-Agent");
        String location = request.getHeader("X-Forwarded-For");
        if (location == null || location.isBlank()) {
            location = request.getRemoteAddr();
        }
        Optional<ShortUrl> shortUrlOptional = urlShortenerService.getOriginalUrl(shortCode, device, location);
        
        if (shortUrlOptional.isPresent()) {
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(shortUrlOptional.get().getMapping().getOriginalUrl());
            return redirectView;
        }
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found for short code: " + shortCode);
    }
}
