package com.github.al.realworld.api.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@JsonRootName("article")
public class CreateArticle implements Command<CreateArticleResult> {

    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String body;
    private List<String> tagList;

}
