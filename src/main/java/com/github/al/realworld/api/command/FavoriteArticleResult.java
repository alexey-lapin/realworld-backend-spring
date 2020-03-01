package com.github.al.realworld.api.command;

import com.github.al.realworld.api.ArticleDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FavoriteArticleResult {

    private ArticleDto article;

}
