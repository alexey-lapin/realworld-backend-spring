package com.github.al.realworld.service;

import com.github.al.realworld.command.ArticleCreateCommand;
import com.github.al.realworld.web.dto.ArticleDto;
import com.github.al.realworld.web.dto.ArticlesDto;

public interface ArticleService {

    ArticleDto create(ArticleCreateCommand cmd);

    ArticlesDto findAll();

}
