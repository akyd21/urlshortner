package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {

    @NotBlank(message = "URL must not be blank")
    @URL(message = "Invalid URL format")
    private String url;

    @Pattern(regexp = "^$|^[A-Za-z0-9_-]{3,32}$", message = "Alias must be 3-32 letters, numbers, hyphens, or underscores")
    private String alias;

    private LocalDateTime expiresAt;
}
