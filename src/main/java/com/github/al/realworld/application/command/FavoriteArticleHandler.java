package com.github.al.realworld.application.command;

import com.github.al.realworld.api.ArticleDto;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.command.FavoriteArticle;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FavoriteArticleHandler implements CommandHandler<FavoriteArticleResult, FavoriteArticle> {

    private final ArticleRepository articleRepository;

    @Transactional
    @Override
    public FavoriteArticleResult handle(FavoriteArticle command) {
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        article.addFavorite(command.getProfile());
        Article savedArticle = articleRepository.save(article);

        return new FavoriteArticleResult(ArticleDto.builder()
                .slug(savedArticle.getSlug())
                .title(savedArticle.getTitle())
                .description(savedArticle.getDescription())
                .createdAt(savedArticle.getCreatedAt())
                .updatedAt(savedArticle.getUpdatedAt())
                .body(savedArticle.getBody())
                .favorited(savedArticle.getFavoritedProfiles().contains(command.getProfile()))
                .favoritesCount(savedArticle.getFavoritedProfiles().size())
                .build());
    }
}
