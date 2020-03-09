package com.github.al.realworld.application.command.comment;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.application.CommentAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Comment;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AddCommentHandler implements CommandHandler<AddCommentResult, AddComment> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public AddCommentResult handle(AddComment command) {
        Article article = articleRepository.findBySlug(command.getSlug()).orElseThrow(() -> new RuntimeException());
        User user = userRepository.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException());

        ZonedDateTime now = ZonedDateTime.now();

        Comment comment = Comment.builder()
                .body(command.getBody())
                .createdAt(now)
                .updatedAt(now)
                .author(user.getProfile())
                .build();
        article.addComment(comment);
        articleRepository.save(article);

        return new AddCommentResult(CommentAssembler.assemble(comment));
    }
}
