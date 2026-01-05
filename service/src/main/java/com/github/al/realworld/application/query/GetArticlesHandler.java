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

import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.api.query.GetArticles;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.model.Tag;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.TagRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GetArticlesHandler implements QueryHandler<GetArticlesResult, GetArticles> {

    private final AuthenticationService authenticationService;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ConversionService conversionService;

    @Transactional(readOnly = true)
    @Override
    public GetArticlesResult handle(GetArticles query) {
        var currentUserId = authenticationService.getCurrentUserId();

        Long tagId;
        if (query.tag() == null) {
            tagId = null;
        } else {
            tagId = tagRepository.findByName(query.tag())
                    .map(Tag::id)
                    .orElse(null);
            if (tagId == null) {
                return new GetArticlesResult(List.of(), 0L);
            }
        }

        Long authorId;
        if (query.author() == null) {
            authorId = null;
        } else {
            authorId = userRepository.findByUsername(query.author())
                    .map(User::id)
                    .orElse(null);
            if (authorId == null) {
                return new GetArticlesResult(List.of(), 0L);
            }
        }

        Long favoritedById;
        if (query.favorited() == null) {
            favoritedById = null;
        } else {
            favoritedById = userRepository.findByUsername(query.favorited())
                    .map(User::id)
                    .orElse(null);
            if (favoritedById == null) {
                return new GetArticlesResult(List.of(), 0L);
            }
        }

        var articles = articleRepository.findAllAssemblyByFilters(currentUserId,
                tagId,
                authorId,
                favoritedById,
                query.limit(),
                query.offset());

        var results = articles.stream()
                .map(a -> conversionService.convert(a, ArticleDto.class))
                .toList();

        var count = articleRepository.countByFilters(tagId, authorId, favoritedById);

        return new GetArticlesResult(results, count);
    }

}
