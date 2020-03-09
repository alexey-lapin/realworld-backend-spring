package com.github.al.realworld.application.command.comment;

import com.github.al.realworld.api.command.DeleteCommentResult;
import com.github.al.realworld.api.command.DeleteComment;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Comment;
import com.github.al.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteCommentHandler implements CommandHandler<DeleteCommentResult, DeleteComment> {

    private final ArticleRepository articleRepository;

    @Transactional
    @Override
    public DeleteCommentResult handle(DeleteComment command) {
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());

        Comment comment = article.getComments().stream()
                .filter(c -> c.getId().equals(command.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException());

        article.removeComment(comment);

        return new DeleteCommentResult();
    }
}
