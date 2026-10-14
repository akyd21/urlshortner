package com.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOM_ALIAS")
@Getter
@Setter
@NoArgsConstructor
public class CustomAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alias_id")
    private Long aliasId;

    @OneToOne
    @JoinColumn(name = "short_url_id", nullable = false, unique = true)
    private ShortUrl shortUrl;

    @Column(name = "alias_text", nullable = false, unique = true, length = 32)
    private String aliasText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public CustomAlias(ShortUrl shortUrl, String aliasText) {
        this.shortUrl = shortUrl;
        this.aliasText = aliasText;
        this.createdAt = LocalDateTime.now();
    }
}
