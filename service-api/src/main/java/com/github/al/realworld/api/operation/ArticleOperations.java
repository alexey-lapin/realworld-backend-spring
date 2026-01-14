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
package com.github.al.realworld.api.operation;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.api.command.UnfavoriteArticleResult;
import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.command.UpdateArticleResult;
import com.github.al.realworld.api.query.GetArticleResult;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.api.query.GetCommentsResult;
import com.github.al.realworld.api.query.GetFeedResult;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface ArticleOperations {

    @GetExchange("/articles")
    GetArticlesResult findByFilters(@RequestParam(required = false) @Nullable String tag,
                                    @RequestParam(required = false) @Nullable String author,
                                    @RequestParam(required = false) @Nullable String favorited,
                                    @RequestParam(defaultValue = "20") int limit,
                                    @RequestParam(defaultValue = "0") long offset);

    @ResponseStatus(HttpStatus.CREATED)
    @PostExchange("/articles")
    CreateArticleResult create(@Valid @RequestBody CreateArticle command);

    @GetExchange("/articles/feed")
    GetFeedResult feed(@RequestParam(defaultValue = "20") int limit,
                       @RequestParam(defaultValue = "0") long offset);

    @GetExchange("/articles/{slug}")
    GetArticleResult findBySlug(@PathVariable("slug") String slug);

    @PutExchange("/articles/{slug}")
    UpdateArticleResult updateBySlug(@PathVariable("slug") String slug,
                                     @Valid @RequestBody UpdateArticle command);

    @DeleteExchange("/articles/{slug}")
    void deleteBySlug(@PathVariable("slug") String slug);

    @PostExchange("/articles/{slug}/favorite")
    FavoriteArticleResult favorite(@PathVariable("slug") String slug);

    @DeleteExchange("/articles/{slug}/favorite")
    UnfavoriteArticleResult unfavorite(@PathVariable("slug") String slug);

    @GetExchange("/articles/{slug}/comments")
    GetCommentsResult findAllComments(@PathVariable("slug") String slug);

    @PostExchange("/articles/{slug}/comments")
    AddCommentResult addComment(@PathVariable("slug") String slug,
                                @Valid @RequestBody AddComment data);

    @DeleteExchange("/articles/{slug}/comments/{id}")
    void deleteComment(@PathVariable("slug") String slug,
                       @PathVariable("id") long id);

}
