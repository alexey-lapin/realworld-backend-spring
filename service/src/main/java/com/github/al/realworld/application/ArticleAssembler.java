package com.github.al.realworld.application;

import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.Tag;

import java.util.stream.Collectors;

public class ArticleAssembler {

    public static ArticleDto assemble(Article article, Profile profile) {
        return ArticleDto.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .tagList(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .favorited(profile != null && article.getFavoritedProfiles().contains(profile))
                .favoritesCount(article.getFavoritedProfiles().size())
                .author(ProfileAssembler.assemble(article.getAuthor()))
                .build();
    }

}
