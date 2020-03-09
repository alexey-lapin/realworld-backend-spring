package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.FavoriteArticle;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FavoriteArticleHandler implements CommandHandler<FavoriteArticleResult, FavoriteArticle> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public FavoriteArticleResult handle(FavoriteArticle command) {

        User user = userRepository.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException());
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        article.addFavorite(user.getProfile());
        Article savedArticle = articleRepository.save(article);

        return new FavoriteArticleResult(ArticleAssembler.assemble(savedArticle, user.getProfile()));
    }
}
