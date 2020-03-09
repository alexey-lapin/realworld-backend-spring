package com.github.al.realworld.api.command;

import com.github.al.realworld.bus.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnfavoriteArticle implements Command<UnfavoriteArticleResult> {

    private String slug;
    private String username;

}
