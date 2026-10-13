package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UrlShortenerService urlShortenerService;

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("request", new ShortenUrlRequest());
        return "index";
    }

    @PostMapping("/shorten")
    public String shortenUrl(@Valid @ModelAttribute("request") ShortenUrlRequest request,
                             BindingResult bindingResult,
                             Model model,
                             HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getUrl());
        
        // Construct the full short URL dynamically based on the current request
        String appUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        String fullShortUrl = appUrl + "/" + shortUrl.getShortCode();
        
        model.addAttribute("shortUrl", fullShortUrl);
        model.addAttribute("originalUrl", shortUrl.getOriginalUrl());
        return "index";
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginal(@PathVariable String shortCode) {
        Optional<ShortUrl> shortUrlOptional = urlShortenerService.getOriginalUrl(shortCode);
        
        if (shortUrlOptional.isPresent()) {
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(shortUrlOptional.get().getOriginalUrl());
            return redirectView;
        }
        
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found for short code: " + shortCode);
    }
}
