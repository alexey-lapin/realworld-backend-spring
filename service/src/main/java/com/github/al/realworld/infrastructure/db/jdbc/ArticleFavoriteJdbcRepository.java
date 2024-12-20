package com.github.al.realworld.infrastructure.db.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ArticleFavoriteJdbcRepository extends Repository<ArticleFavoriteJdbcEntity, ArticleFavoriteJdbcEntity> {

    @Query("SELECT EXISTS(SELECT 1 FROM TBL_ARTICLE_FAVORITES WHERE ARTICLE_ID = :articleId AND USER_ID = :userId)")
    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);

    int countByArticleId(UUID articleId);

    @Modifying
    @Query("INSERT INTO TBL_ARTICLE_FAVORITES (ARTICLE_ID, USER_ID) VALUES (:articleId, :userId)")
    void save(UUID articleId, UUID userId);

    @Modifying
    @Query("DELETE FROM TBL_ARTICLE_FAVORITES WHERE ARTICLE_ID = :articleId AND USER_ID = :userId")
    void deleteByArticleIdAndUserId(UUID articleId, UUID userId);

}
