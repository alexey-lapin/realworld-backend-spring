package com.github.al.realworld.domain.repository;

import com.github.al.realworld.domain.model.ArticleFavorite;

import java.util.UUID;

public interface ArticleFavoriteRepository {

    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);

    int countByArticleId(UUID articleId);

    List<>

    ArticleFavorite save(ArticleFavorite articleFavorite);

    void deleteByArticleIdAndUserId(UUID articleId, UUID userId);

}
