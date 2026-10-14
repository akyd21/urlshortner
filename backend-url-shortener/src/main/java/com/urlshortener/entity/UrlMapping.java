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

@Entity
@Table(name = "URL_MAPPING")
@Getter
@Setter
@NoArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @OneToOne
    @JoinColumn(name = "short_url_id", nullable = false, unique = true)
    private ShortUrl shortUrl;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(length = 255)
    private String domain;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public UrlMapping(ShortUrl shortUrl, String originalUrl, String domain) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.domain = domain;
    }
}
