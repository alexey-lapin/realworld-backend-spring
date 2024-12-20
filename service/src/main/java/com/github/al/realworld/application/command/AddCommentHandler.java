/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.application.CommentAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.model.Comment;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.CommentRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.github.al.realworld.application.exception.BadRequestException.badRequest;
import static com.github.al.realworld.application.exception.NotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class AddCommentHandler implements CommandHandler<AddCommentResult, AddComment> {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public AddCommentResult handle(AddComment command) {
        UUID articleId = articleRepository.findIdBySlug(command.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exist", command.getSlug()));

        User currentUser = userRepository.findByUsername(command.getCurrentUsername())
                .orElseThrow(() -> badRequest("user [name=%s] does not exist", command.getCurrentUsername()));

        ZonedDateTime now = ZonedDateTime.now();

        Comment comment = Comment.builder()
                .articleId(articleId)
                .body(command.getBody())
                .createdAt(now)
                .updatedAt(now)
                .authorId(currentUser.getId())
                .build();

        Comment savedComment = commentRepository.save(comment);

        return new AddCommentResult(CommentAssembler.assemble(savedComment, currentUser));
    }

}
