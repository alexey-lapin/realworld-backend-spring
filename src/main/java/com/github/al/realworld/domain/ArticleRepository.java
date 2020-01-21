package com.github.al.realworld.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ArticleRepository extends CrudRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findByTitle(String title);

}
