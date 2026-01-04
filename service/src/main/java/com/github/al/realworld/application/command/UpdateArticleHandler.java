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

import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.command.UpdateArticleResult;
import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.application.service.SlugService;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.ForbiddenException.forbidden;
import static com.github.al.realworld.application.exception.NotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class UpdateArticleHandler implements CommandHandler<UpdateArticleResult, UpdateArticle> {

    private final AuthenticationService authenticationService;
    private final ArticleRepository articleRepository;
    private final SlugService slugService;
    private final ConversionService conversionService;

    @Transactional
    @Override
    public UpdateArticleResult handle(UpdateArticle command) {
        var currentUserId = authenticationService.getRequiredCurrentUserId();

        var article = articleRepository.findBySlug(command.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exist", command.getSlug()));

        if (article.authorId() != currentUserId) {
            throw forbidden("article [slug=%s] is not owned by %s",
                    command.getSlug(), authenticationService.getCurrentUserName());
        }

        var alteredArticle = article.toBuilder()
                .slug(command.getTitle() != null ? slugService.makeSlug(command.getTitle()) : article.slug())
                .title(command.getTitle() != null ? command.getTitle() : article.title())
                .description(command.getDescription() != null ? command.getDescription() : article.description())
                .body(command.getBody() != null ? command.getBody() : article.body())
                .build();

        articleRepository.save(alteredArticle);

        var articleAssembly = articleRepository.findAssemblyById(currentUserId, article.id()).orElseThrow();
        var data = conversionService.convert(articleAssembly, ArticleDto.class);

        return new UpdateArticleResult(data);
    }

}
