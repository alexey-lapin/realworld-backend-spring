package com.github.al.realworld.api.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@JsonRootName("article")
public class UpdateArticle implements Command<UpdateArticleResult> {

    private String slug;
    private String title;
    private String description;
    private String body;
    private String username;

}
