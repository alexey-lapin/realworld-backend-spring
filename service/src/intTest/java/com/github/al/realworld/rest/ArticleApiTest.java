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
package com.github.al.realworld.rest;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.api.operation.ArticleClient;
import com.github.al.realworld.rest.auth.AuthSupport;
import com.github.al.realworld.rest.support.FeignBasedRestTest;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ArticleApiTest extends FeignBasedRestTest {

    public static final String TEST_TITLE = "test-title";
    public static final String TEST_DESCRIPTION = "test-description";
    public static final String TEST_BODY = "test-body";
    public static final String ALTERED_TITLE = "altered-title";
    public static final String ALTERED_BODY = "altered-body";
    public static final String ALTERED_DESCRIPTION = "altered-description";

    @Autowired
    private AuthSupport auth;

    @Autowired
    private ArticleClient articleClient;

    @AfterEach
    void afterEach() {
        auth.logout();
    }

    @Test
    void should_returnCorrectArticleData() {
        String user = auth.register().login().getUsername();

        CreateArticle command = createArticleCommand();
        ArticleDto article = articleClient.create(command).getArticle();

        assertThat(article.getSlug()).isEqualTo(command.getTitle());
        assertThat(article.getTitle()).isEqualTo(command.getTitle());
        assertThat(article.getDescription()).isEqualTo(command.getDescription());
        assertThat(article.getBody()).isEqualTo(command.getBody());
        assertThat(article.getAuthor().getUsername()).isEqualTo(user);
    }

    @Test
    void should_returnCorrectArticleData_when_favoriteAndUnfavorite() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).getArticle();

        ArticleDto favoritedArticle = articleClient.favorite(created.getSlug()).getArticle();
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(favoritedArticle.getFavoritesCount()).isEqualTo(1);

        ArticleDto unfavoritedArticle = articleClient.unfavorite(created.getSlug()).getArticle();
        assertThat(unfavoritedArticle.getFavorited()).isFalse();
        assertThat(unfavoritedArticle.getFavoritesCount()).isEqualTo(0);
    }

    @Test
    void should_returnCorrectArticleData_when_delete() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).getArticle();

        articleClient.deleteBySlug(created.getSlug());

        FeignException exception = catchThrowableOfType(
                () -> articleClient.findBySlug(created.getSlug()),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    void should_throw403_when_deleteNotOwned() {
        auth.register().login();

        ArticleDto created = articleClient.create(createArticleCommand()).getArticle();

        auth.register().login();

        FeignException exception = catchThrowableOfType(
                () -> articleClient.deleteBySlug(created.getSlug()),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void should_returnCorrectArticleData_when_deleteNotExisting() {
        auth.register().login();

        FeignException exception = catchThrowableOfType(
                () -> articleClient.deleteBySlug("not-existing"),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void should_returnCorrectArticleData_when_update() {
        auth.register().login();

        ArticleDto created = articleClient.create(createArticleCommand()).getArticle();

        UpdateArticle updateCommand = UpdateArticle.builder()
                .title(ALTERED_TITLE)
                .body(ALTERED_BODY)
                .description(ALTERED_DESCRIPTION)
                .build();

        ArticleDto updated = articleClient.updateBySlug(created.getSlug(), updateCommand).getArticle();

        assertThat(updated.getSlug()).isEqualTo(ALTERED_TITLE);
        assertThat(updated.getTitle()).isEqualTo(ALTERED_TITLE);
        assertThat(updated.getDescription()).isEqualTo(ALTERED_DESCRIPTION);
        assertThat(updated.getBody()).isEqualTo(ALTERED_BODY);
    }

    @Test
    void shouldReturnCorrectCommentData_whenCreateDeleteComment() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).getArticle();

        AddComment addComment = AddComment.builder().body(TEST_BODY).build();

        CommentDto comment = articleClient.addComment(created.getSlug(), addComment).getComment();

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getAuthor().getUsername()).isEqualTo(user);
        assertThat(comment.getBody()).isEqualTo(TEST_BODY);
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isNotNull();

        List<CommentDto> comments = articleClient.findAllComments(created.getSlug()).getComments();

        assertThat(comments).hasSize(1);

        articleClient.deleteComment(created.getSlug(), comment.getId());

        comments = articleClient.findAllComments(created.getSlug()).getComments();

        assertThat(comments).hasSize(1);
    }

    @Test
    void should_throw403_when_commentIsNotOwned() {
        auth.register().login();

        ArticleDto article = articleClient.create(createArticleCommand()).getArticle();

        AddComment addComment = AddComment.builder().body(TEST_BODY).build();
        CommentDto comment = articleClient.addComment(article.getSlug(), addComment).getComment();

        auth.register().login();

        FeignException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(article.getSlug(), comment.getId()),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void should_throw404_when_addCommentArticleDoesNotExist() {
        auth.register().login();

        AddComment addComment = AddComment.builder().body(TEST_BODY).build();
        FeignException exception = catchThrowableOfType(
                () -> articleClient.addComment(UUID.randomUUID().toString(), addComment),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void should_throw404_when_deleteCommentArticleDoesNotExist() {
        auth.register().login();

        FeignException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(UUID.randomUUID().toString(), 9999999L),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void should_throw404_when_deleteCommentDoesNotExist() {
        auth.register().login();

        ArticleDto article = articleClient.create(createArticleCommand()).getArticle();

        FeignException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(article.getSlug(), 9999999L),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private static CreateArticle createArticleCommand() {
        return CreateArticle.builder()
                .title(UUID.randomUUID().toString())
                .description(UUID.randomUUID().toString())
                .body(UUID.randomUUID().toString())
                .build();
    }

}
