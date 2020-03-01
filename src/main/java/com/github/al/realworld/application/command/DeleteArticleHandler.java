package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.DeleteArticle;
import com.github.al.realworld.api.command.DeleteArticleResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeleteArticleHandler implements CommandHandler<DeleteArticleResult, DeleteArticle> {

    private final ArticleRepository articleRepository;

    @Override
    public DeleteArticleResult handle(DeleteArticle command) {
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        articleRepository.delete(article);
        return new DeleteArticleResult();
    }
}
