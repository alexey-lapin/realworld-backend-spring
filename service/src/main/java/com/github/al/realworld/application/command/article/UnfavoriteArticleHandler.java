package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.UnfavoriteArticle;
import com.github.al.realworld.api.command.UnfavoriteArticleResult;
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
public class UnfavoriteArticleHandler implements CommandHandler<UnfavoriteArticleResult, UnfavoriteArticle> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UnfavoriteArticleResult handle(UnfavoriteArticle command) {
        User user = userRepository.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException());
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        article.removeFavorite(user.getProfile());
        Article savedArticle = articleRepository.save(article);

        return new UnfavoriteArticleResult(ArticleAssembler.assemble(savedArticle, user.getProfile()));
    }
}
