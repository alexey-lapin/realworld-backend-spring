package com.github.al.realworld.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ArticleDetails {

    private String slug;
    private String title;
    private String description;
    private String body;
    private List<String> tagList;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Boolean favorited;
    private Integer favoritesCount;
    //author

}
