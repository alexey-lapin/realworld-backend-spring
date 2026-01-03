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
package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.api.query.GetComment;
import com.github.al.realworld.api.query.GetCommentResult;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.CommentRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.NotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class GetCommentHandler implements QueryHandler<GetCommentResult, GetComment> {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ConversionService conversionService;

    @Transactional(readOnly = true)
    @Override
    public GetCommentResult handle(GetComment query) {
        var currentUsername = query.getCurrentUsername();
        Long currentUserId;
        if (currentUsername == null) {
            currentUserId = null;
        } else {
            currentUserId = userRepository.findByUsername(currentUsername)
                    .map(User::id)
                    .orElse(null);
        }

        var commentAssembly = commentRepository.findAssemblyById(currentUserId, query.getId())
                .orElseThrow(() -> notFound("comment [id=%s] does not exists", query.getId()));
        var data = conversionService.convert(commentAssembly, CommentDto.class);

        return new GetCommentResult(data);
    }

}
