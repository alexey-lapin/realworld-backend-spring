package com.github.al.realworld.application.command;

import com.github.al.realworld.api.ArticleDto;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.api.command.UnfavoriteArticle;
import com.github.al.realworld.api.command.UnfavoriteArticleResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UnfavoriteArticleHandler implements CommandHandler<UnfavoriteArticleResult, UnfavoriteArticle> {

    private final ArticleRepository articleRepository;

    @Override
    public UnfavoriteArticleResult handle(UnfavoriteArticle command) {
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        article.removeFavorite(command.getProfile());
        Article savedArticle = articleRepository.save(article);

        return new UnfavoriteArticleResult(ArticleDto.builder()
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
