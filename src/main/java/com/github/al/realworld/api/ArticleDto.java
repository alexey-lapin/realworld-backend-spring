package com.github.al.realworld.api;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Getter
public class ArticleDto {

    private String slug;
    private String title;
    private String description;
    private String body;
    private List<String> tagList;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean favorited;
    private Integer favoritesCount;
    //author

}
