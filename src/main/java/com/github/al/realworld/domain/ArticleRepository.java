package com.github.al.realworld.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends CrudRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findByTitle(String title);

    @Query("SELECT a FROM Article a JOIN Tag t JOIN Profile p WHERE t.name = :tag AND p.username = :author AND p.favorites")
    List<Article> findByFilters(@Param("tag") String tag, @Param("author") String author, @Param("favorited") String favorited);

}
