package com.github.al.realworld.api.command;

import com.github.al.realworld.bus.Command;
import com.github.al.realworld.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnfavoriteArticle implements Command<UnfavoriteArticleResult> {

    private String slug;
    private Profile profile;

}
