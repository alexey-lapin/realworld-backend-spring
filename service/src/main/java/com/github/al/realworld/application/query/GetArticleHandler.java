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
import com.github.al.realworld.api.query.GetArticle;
import com.github.al.realworld.api.query.GetArticleResult;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.model.Article;
import com.github.al.realworld.domain.model.ArticleAssembly;
import com.github.al.realworld.domain.model.ProfileAssembly;
import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.ArticleFavoriteRepository;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.FollowRelationRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.NotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class GetArticleHandler implements QueryHandler<GetArticleResult, GetArticle> {

    private final ArticleRepository articleRepository;
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final FollowRelationRepository followRelationRepository;
    private final UserRepository userRepository;
    private final ConversionService conversionService;

    @Transactional(readOnly = true)
    @Override
    public GetArticleResult handle(GetArticle query) {
        Article article = articleRepository.findBySlug(query.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exists", query.getSlug()));

        User currentUser = userRepository.findByUsername(query.getCurrentUsername()).orElse(null);
        User author = userRepository.findById(article.getAuthorId()).orElse(null);

        boolean following = false;
        if (currentUser != null && author != null) {
            following = followRelationRepository.existsByFollowerIdAndFolloweeId(currentUser.getId(), author.getId());
        }

        boolean favorited = false;
        if (currentUser != null) {
            favorited = articleFavoriteRepository.existsByArticleIdAndUserId(article.getId(), currentUser.getId());
        }

        int favoriteCount = articleFavoriteRepository.countByArticleId(article.getId());

        ProfileAssembly profileAssembly = new ProfileAssembly(author, following);
        ArticleAssembly articleAssembly = new ArticleAssembly(article, favorited, favoriteCount, profileAssembly);
        ArticleDto data = conversionService.convert(articleAssembly, ArticleDto.class);

        return new GetArticleResult(data);
    }

}
