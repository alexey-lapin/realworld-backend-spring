package com.github.al.realworld.application.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@JsonRootName("article")
public class ArticleCreateCommand {

    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String body;
    private List<String> tagList;
}
