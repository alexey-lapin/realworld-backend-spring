package com.github.al.realworld.api.command;

import com.github.al.realworld.bus.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FavoriteArticle implements Command<FavoriteArticleResult> {

    private String slug;
    private String username;

}
