package com.github.al.realworld.infrastructure.db.jdbc;

import com.github.al.realworld.domain.model.ArticleFavorite;
import com.github.al.realworld.domain.repository.ArticleFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class ArticleFavoriteJdbcRepositoryAdapter implements ArticleFavoriteRepository {

    private final ArticleFavoriteJdbcRepository repository;

    @Override
    public boolean existsByArticleIdAndUserId(UUID articleId, UUID userId) {
        return repository.existsByArticleIdAndUserId(articleId, userId);
    }

    @Override
    public int countByArticleId(UUID articleId) {
        return repository.countByArticleId(articleId);
    }

    @Override
    public ArticleFavorite save(ArticleFavorite articleFavorite) {
        repository.save(articleFavorite.getArticleId(), articleFavorite.getUserId());
        return articleFavorite;
    }

    @Override
    public void deleteByArticleIdAndUserId(UUID articleId, UUID userId) {
        repository.deleteByArticleIdAndUserId(articleId, userId);
    }

}
