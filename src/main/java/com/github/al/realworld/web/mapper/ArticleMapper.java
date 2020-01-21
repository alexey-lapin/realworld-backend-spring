package com.github.al.realworld.web.mapper;

import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Tag;
import com.github.al.realworld.web.dto.ArticleDetails;
import com.github.al.realworld.web.dto.ArticleDto;
import com.github.al.realworld.web.dto.ArticlesDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ArticleMapper {

    public ArticleDetails toDetails(Article article) {
        return ArticleDetails.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .favorited(null)
                .favoritesCount(null)
                .tagList(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .build();
    }

    public ArticleDto toDto(Article article) {
        return new ArticleDto(toDetails(article));
    }

    public ArticlesDto toListDto(Iterable<Article> articles) {
        List<ArticleDetails> articlesDetailsList = StreamSupport.stream(articles.spliterator(), false)
                .map(this::toDetails)
                .collect(Collectors.toList());
        return new ArticlesDto(articlesDetailsList);
    }

}
