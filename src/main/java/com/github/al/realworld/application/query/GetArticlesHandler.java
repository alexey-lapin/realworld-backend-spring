package com.github.al.realworld.application.query;

import com.github.al.realworld.api.ArticleDto;
import com.github.al.realworld.api.query.GetArticles;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GetArticlesHandler implements QueryHandler<GetArticlesResult, GetArticles> {

    private final ArticleRepository articleRepository;

    @Override
    public GetArticlesResult handle(GetArticles query) {
        List<Article> articles = articleRepository.findByFilters(query.getTag(), query.getAuthor(), query.getFavorited());
        List<ArticleDto> results = new ArrayList<>();
        articles.forEach(article -> results.add(ArticleDto.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .body(article.getBody())
                .build()));
        return new GetArticlesResult(results);
    }
}
