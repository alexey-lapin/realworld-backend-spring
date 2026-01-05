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
import com.github.al.realworld.rest.support.BaseRestTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ArticleApiTest extends BaseRestTest {

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
        ArticleDto article = articleClient.create(command).article();

        assertThat(article.slug()).isEqualTo(command.article().title());
        assertThat(article.title()).isEqualTo(command.article().title());
        assertThat(article.description()).isEqualTo(command.article().description());
        assertThat(article.body()).isEqualTo(command.article().body());
        assertThat(article.author().username()).isEqualTo(user);
    }

    @Test
    void should_returnCorrectArticleData_when_favoriteAndUnfavorite() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).article();

        ArticleDto favoritedArticle = articleClient.favorite(created.slug()).article();
        assertThat(favoritedArticle.favorited()).isTrue();
        assertThat(favoritedArticle.favoritesCount()).isEqualTo(1);

        ArticleDto unfavoritedArticle = articleClient.unfavorite(created.slug()).article();
        assertThat(unfavoritedArticle.favorited()).isFalse();
        assertThat(unfavoritedArticle.favoritesCount()).isEqualTo(0);
    }

    @Test
    void should_returnCorrectArticleData_when_delete() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).article();

        articleClient.deleteBySlug(created.slug());

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.findBySlug(created.slug()),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void should_throw403_when_deleteNotOwned() {
        auth.register().login();

        ArticleDto created = articleClient.create(createArticleCommand()).article();

        auth.register().login();

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.deleteBySlug(created.slug()),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_returnCorrectArticleData_when_deleteNotExisting() {
        auth.register().login();

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.deleteBySlug("not-existing"),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_returnCorrectArticleData_when_update() {
        auth.register().login();

        ArticleDto created = articleClient.create(createArticleCommand()).article();

        UpdateArticle updateCommand = new UpdateArticle(null, new UpdateArticle.Data(
                ALTERED_TITLE,
                ALTERED_DESCRIPTION,
                ALTERED_BODY
        ));

        ArticleDto updated = articleClient.updateBySlug(created.slug(), updateCommand).article();

        assertThat(updated.slug()).isEqualTo(ALTERED_TITLE);
        assertThat(updated.title()).isEqualTo(ALTERED_TITLE);
        assertThat(updated.description()).isEqualTo(ALTERED_DESCRIPTION);
        assertThat(updated.body()).isEqualTo(ALTERED_BODY);
    }

    @Test
    void shouldReturnCorrectCommentData_whenCreateDeleteComment() {
        String user = auth.register().login().getUsername();

        ArticleDto created = articleClient.create(createArticleCommand()).article();

        AddComment addComment = new AddComment(null, new AddComment.Data(TEST_BODY));

        CommentDto comment = articleClient.addComment(created.slug(), addComment).comment();

        assertThat(comment.id()).isNotNull();
        assertThat(comment.author().username()).isEqualTo(user);
        assertThat(comment.body()).isEqualTo(TEST_BODY);
        assertThat(comment.createdAt()).isNotNull();
        assertThat(comment.updatedAt()).isNotNull();

        List<CommentDto> comments = articleClient.findAllComments(created.slug()).comments();

        assertThat(comments).hasSize(1);

        articleClient.deleteComment(created.slug(), comment.id());

        comments = articleClient.findAllComments(created.slug()).comments();

        assertThat(comments).hasSize(0);
    }

    @Test
    void should_throw403_when_commentIsNotOwned() {
        auth.register().login();

        ArticleDto article = articleClient.create(createArticleCommand()).article();

        AddComment addComment = new AddComment(null, new AddComment.Data(TEST_BODY));
        CommentDto comment = articleClient.addComment(article.slug(), addComment).comment();

        auth.register().login();

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(article.slug(), comment.id()),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_throw404_when_addCommentArticleDoesNotExist() {
        auth.register().login();

        AddComment addComment = new AddComment(null, new AddComment.Data(TEST_BODY));
        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.addComment(UUID.randomUUID().toString(), addComment),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_throw404_when_deleteCommentArticleDoesNotExist() {
        auth.register().login();

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(UUID.randomUUID().toString(), 9999999L),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_throw404_when_deleteCommentDoesNotExist() {
        auth.register().login();

        ArticleDto article = articleClient.create(createArticleCommand()).article();

        RestClientResponseException exception = catchThrowableOfType(
                () -> articleClient.deleteComment(article.slug(), 9999999L),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private static CreateArticle createArticleCommand() {
        return new CreateArticle(new CreateArticle.Data(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null
        ));
    }

}
