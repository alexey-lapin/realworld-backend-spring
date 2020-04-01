package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.Article;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringArticleRepository extends CrudRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findByTitle(String title);

    @Query("SELECT DISTINCT a FROM Article a LEFT JOIN a.tags t LEFT JOIN a.author p LEFT JOIN a.favoritedProfiles f WHERE " +
            "(:tag IS NULL OR t.name = :tag) AND " +
            "(:author IS NULL OR p.username = :author) AND " +
            "(:favorited IS NULL OR f.username = :favorited)")
    List<Article> findByFilters(@Param("tag") String tag,
                                @Param("author") String author,
                                @Param("favorited") String favorited);

    @Query("SELECT a FROM Article a JOIN a.author au WHERE au.username IN :followees")
    List<Article> findByFollowees(@Param("followees") List<String> followees);

}
