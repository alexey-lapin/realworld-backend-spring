package com.github.al.realworld.infrastructure.db.jdbc;

import com.github.al.realworld.domain.model.ArticleFavorite;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Table("TBL_ARTICLE_FAVORITES")
public class ArticleFavoriteJdbcEntity implements Persistable<ArticleFavoriteJdbcEntity> {

    private final UUID articleId;
    private final UUID userId;

    @Override
    public ArticleFavoriteJdbcEntity getId() {
        return this;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public ArticleFavorite toDomain() {
        return new ArticleFavorite(articleId, userId);
    }

    public static ArticleFavoriteJdbcEntity fromDomain(ArticleFavorite domain) {
        return new ArticleFavoriteJdbcEntity(domain.getArticleId(), domain.getUserId());
    }

}
