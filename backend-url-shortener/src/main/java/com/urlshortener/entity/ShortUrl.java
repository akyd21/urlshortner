package com.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SHORT_URL")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "short_url_id")
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 32)
    private String shortCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToOne(mappedBy = "shortUrl", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true, optional = false)
    private UrlMapping mapping;

    @OneToOne(mappedBy = "shortUrl", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true)
    private CustomAlias customAlias;

    @OneToMany(mappedBy = "shortUrl", cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<ClickEvent> clickEvents = new ArrayList<>();

    public ShortUrl(Long id, String shortCode, LocalDateTime createdAt, LocalDateTime expiresAt,
                    String status, User owner,
                    UrlMapping mapping, CustomAlias customAlias,
                    List<ClickEvent> clickEvents) {
        this.id = id;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.owner = owner;
        this.mapping = mapping;
        this.customAlias = customAlias;
        this.clickEvents = clickEvents == null ? new ArrayList<>() : clickEvents;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusMonths(3);
        }
    }

    public String getOriginalUrl() {
        return mapping == null ? null : mapping.getOriginalUrl();
    }
}
