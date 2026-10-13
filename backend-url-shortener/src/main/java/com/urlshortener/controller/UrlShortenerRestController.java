package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;

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
        String fullShortUrl = appUrl + "/" + shortUrl.getShortCode();
        
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", fullShortUrl);
        response.put("originalUrl", shortUrl.getOriginalUrl());
        response.put("shortCode", shortUrl.getShortCode());
        response.put("clickCount", String.valueOf(shortUrl.getClickCount()));
        response.put("expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString());
        
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
                    "originalUrl", shortUrl.getOriginalUrl(),
                    "shortUrl", baseUrl + "/" + shortUrl.getShortCode(),
                    "shortCode", shortUrl.getShortCode());
        }).collect(Collectors.toList());
    }

    @GetMapping("/links/{shortCode}/stats")
    public Map<String, String> stats(@PathVariable String shortCode, Principal principal) {
        ShortUrl shortUrl = urlShortenerService.findOwnedByCode(shortCode, principal.getName());
        return Map.of(
                "shortCode", shortUrl.getShortCode(),
                "originalUrl", shortUrl.getOriginalUrl(),
                "clickCount", String.valueOf(shortUrl.getClickCount()),
                "createdAt", shortUrl.getCreatedAt().toString(),
                "expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString());
    }

    @GetMapping("/links")
    public List<Map<String, String>> links(HttpServletRequest httpRequest, Principal principal) {
        String baseUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        return urlShortenerService.findOwnedLinks(principal.getName()).stream()
                .map(shortUrl -> {
                    Map<String, String> link = new HashMap<>();
                    link.put("shortCode", shortUrl.getShortCode());
                    link.put("shortUrl", baseUrl + "/" + shortUrl.getShortCode());
                    link.put("originalUrl", shortUrl.getOriginalUrl());
                    link.put("clickCount", String.valueOf(shortUrl.getClickCount()));
                    link.put("createdAt", shortUrl.getCreatedAt().toString());
                    link.put("expiresAt", shortUrl.getExpiresAt() == null ? "" : shortUrl.getExpiresAt().toString());
                    link.put("lastClickedAt", shortUrl.getLastClickedAt() == null ? "" : shortUrl.getLastClickedAt().toString());
                    return link;
                })
                .toList();
    }

    @GetMapping(value = "/links/{shortCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrCode(@PathVariable String shortCode, HttpServletRequest httpRequest) throws Exception {
        ShortUrl shortUrl = urlShortenerService.findByCode(shortCode);
        String baseUrl = httpRequest.getRequestURL().toString()
                .replace(httpRequest.getRequestURI(), "");
        BitMatrix matrix = new MultiFormatWriter().encode(
                baseUrl + "/" + shortUrl.getShortCode(),
                BarcodeFormat.QR_CODE, 320, 320);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        return output.toByteArray();
    }
}
