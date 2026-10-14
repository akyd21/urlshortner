package com.urlshortener.repository;

import com.urlshortener.entity.CustomAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomAliasRepository extends JpaRepository<CustomAlias, Long> {

    boolean existsByAliasTextIgnoreCase(String aliasText);

    Optional<CustomAlias> findByAliasTextIgnoreCaseAndActiveTrue(String aliasText);
}
