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

import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.application.service.SlugService;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.model.Article;
import com.github.al.realworld.domain.model.Tag;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.al.realworld.application.exception.BadRequestException.badRequest;

@RequiredArgsConstructor
@Service
public class CreateArticleHandler implements CommandHandler<CreateArticleResult, CreateArticle> {

    private final AuthenticationService authenticationService;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final SlugService slugService;
    private final ConversionService conversionService;

    @Transactional
    @Override
    public CreateArticleResult handle(CreateArticle command) {
        var currentUserId = authenticationService.getRequiredCurrentUserId();
        var articleData = command.article();

        var existsByTitle = articleRepository.existsByTitle(articleData.title());
        if (existsByTitle) {
            throw badRequest("article [title=%s] already exists", articleData.title());
        }

        var tags = handleTags(articleData);

        var article = Article.builder()
                .slug(slugService.makeSlug(articleData.title()))
                .title(articleData.title())
                .description(articleData.description())
                .body(articleData.body())
                .authorId(currentUserId)
                .tags(tags)
                .build();

        var savedArticle = articleRepository.save(article);

        var articleAssembly = articleRepository.findAssemblyById(currentUserId, savedArticle.id()).orElseThrow();
        var data = conversionService.convert(articleAssembly, ArticleDto.class);

        return new CreateArticleResult(data);
    }

    private List<Tag> handleTags(CreateArticle.Data articleData) {
        List<Tag> tags = new ArrayList<>();
        var tagList = articleData.tagList();
        if (tagList != null && !tagList.isEmpty()) {
            var existingTags = tagRepository.findAllByNameIn(tagList);
            var existingTagNames = existingTags.stream()
                    .map(Tag::name)
                    .collect(Collectors.toSet());
            var newTags = tagList.stream()
                    .filter(name -> !existingTagNames.contains(name))
                    .map(Tag::new)
                    .toList();
            for (var newTag : newTags) {
                Tag tag;
                try {
                    tag = tagRepository.save(newTag);
                } catch (DbActionExecutionException ex) {
                    if (ex.getCause() instanceof DuplicateKeyException) {
                        tag = tagRepository.findByName(newTag.name()).orElseThrow();
                    }
                    throw ex;
                }
                tags.add(tag);
            }
            tags.addAll(existingTags);
            Collections.sort(tags);
        }
        return tags;
    }

}
