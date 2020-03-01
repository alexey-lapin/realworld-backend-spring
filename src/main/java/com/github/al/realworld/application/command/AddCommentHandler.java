package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AddCommentHandler implements CommandHandler<AddCommentResult, AddComment> {

    private final ArticleRepository articleRepository;

    @Override
    public AddCommentResult handle(AddComment command) {
        return null;
    }
}
