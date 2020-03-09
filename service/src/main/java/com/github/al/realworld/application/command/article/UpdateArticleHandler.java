package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.command.UpdateArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.application.service.Slugify;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Service
public class UpdateArticleHandler implements CommandHandler<UpdateArticleResult, UpdateArticle> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final Slugify slugify;

    @Transactional
    @Override
    public UpdateArticleResult handle(UpdateArticle command) {
        User user = userRepository.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException());
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());

        Article updatedArticle = article.toBuilder()
                .slug(command.getTitle() != null ? slugify.makeSlug(command.getTitle()) : article.getSlug())
                .title(command.getTitle() != null ? command.getTitle() : article.getTitle())
                .description(command.getDescription() != null ? command.getDescription() : article.getDescription())
                .body(command.getBody() != null ? command.getBody() : article.getBody())
                .updatedAt(ZonedDateTime.now())
                .build();

        articleRepository.save(updatedArticle);

        return new UpdateArticleResult(ArticleAssembler.assemble(updatedArticle, user.getProfile()));
    }
}
