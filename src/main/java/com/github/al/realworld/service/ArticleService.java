package com.github.al.realworld.service;

import com.github.al.realworld.application.command.ArticleCreateCommand;
import com.github.al.realworld.infrastructure.web.dto.ArticleDto;
import com.github.al.realworld.infrastructure.web.dto.ArticlesDto;

public interface ArticleService {

    ArticleDto create(ArticleCreateCommand cmd);

    ArticlesDto findAll();

    void deleteBySlug(String slug);
}
