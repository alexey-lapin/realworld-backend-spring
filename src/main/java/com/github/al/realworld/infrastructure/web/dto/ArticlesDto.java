package com.github.al.realworld.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ArticlesDto {

    private Iterable<ArticleDetails> articles;

}
