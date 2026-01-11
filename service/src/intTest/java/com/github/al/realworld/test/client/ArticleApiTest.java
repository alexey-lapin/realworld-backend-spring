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
package com.github.al.realworld.test.client;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.dto.ArticleItemDto;
import com.github.al.realworld.api.operation.ArticleClient;
import com.github.al.realworld.api.operation.ProfileClient;
import com.github.al.realworld.api.operation.TagClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ArticleApiTest extends BaseClientTest {

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

    @Autowired
    private TagClient tagClient;

    @Autowired
    private ProfileClient profileClient;

    @AfterEach
    void afterEach() {
        auth.logout();
    }

    @Nested
    class ArticleCRUD {

        @Test
        void should_returnCorrectArticleData() {
            var user = auth.register().login().getUsername();

            var command = createArticleCommand();
            var article = articleClient.create(command).article();

            assertThat(article.slug()).matches(Pattern.quote(command.article().title()) + "-[a-z0-9]{12}");
            assertThat(article.title()).isEqualTo(command.article().title());
            assertThat(article.description()).isEqualTo(command.article().description());
            assertThat(article.body()).isEqualTo(command.article().body());
            assertThat(article.author().username()).isEqualTo(user);
        }

        @Test
        void should_returnArticle_when_findBySlug() {
            var user = auth.register().login().getUsername();
            var article = articleClient.create(createArticleCommand()).article();

            var result = articleClient.findBySlug(article.slug());

            assertThat(result.article().slug()).isEqualTo(article.slug());
            assertThat(result.article().title()).isEqualTo(article.title());
            assertThat(result.article().author().username()).isEqualTo(user);
        }

        @Test
        void should_throw404_when_findBySlugDoesNotExist() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.findBySlug("non-existing-slug")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_returnCorrectArticleData_when_update() {
            auth.register().login();

            var created = articleClient.create(createArticleCommand()).article();

            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(
                    ALTERED_TITLE,
                    ALTERED_DESCRIPTION,
                    ALTERED_BODY
            ));

            var updated = articleClient.updateBySlug(created.slug(), updateCommand).article();

            assertThat(updated.slug()).matches(Pattern.quote(ALTERED_TITLE) + "-[a-z0-9]{12}");
            assertThat(updated.title()).isEqualTo(ALTERED_TITLE);
            assertThat(updated.description()).isEqualTo(ALTERED_DESCRIPTION);
            assertThat(updated.body()).isEqualTo(ALTERED_BODY);
        }

        @Test
        void should_throw403_when_updateNotOwned() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();

            auth.register().login();
            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(ALTERED_TITLE, null, null));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.updateBySlug(created.slug(), updateCommand)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void should_throw404_when_updateDoesNotExist() {
            auth.register().login();
            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(ALTERED_TITLE, null, null));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.updateBySlug("non-existing-slug", updateCommand)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_updateOnlyTitle() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();

            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(ALTERED_TITLE, null, null));
            var updated = articleClient.updateBySlug(created.slug(), updateCommand).article();

            assertThat(updated.title()).isEqualTo(ALTERED_TITLE);
            assertThat(updated.description()).isEqualTo(created.description());
            assertThat(updated.body()).isEqualTo(created.body());
        }

        @Test
        void should_updateOnlyDescription() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();

            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(null, ALTERED_DESCRIPTION, null));
            var updated = articleClient.updateBySlug(created.slug(), updateCommand).article();

            assertThat(updated.title()).isEqualTo(created.title());
            assertThat(updated.description()).isEqualTo(ALTERED_DESCRIPTION);
            assertThat(updated.body()).isEqualTo(created.body());
        }

        @Test
        void should_updateOnlyBody() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();

            var updateCommand = new UpdateArticle(null, new UpdateArticle.Data(null, null, ALTERED_BODY));
            var updated = articleClient.updateBySlug(created.slug(), updateCommand).article();

            assertThat(updated.title()).isEqualTo(created.title());
            assertThat(updated.description()).isEqualTo(created.description());
            assertThat(updated.body()).isEqualTo(ALTERED_BODY);
        }

        @Test
        void should_returnCorrectArticleData_when_delete() {
            var user = auth.register().login().getUsername();

            var created = articleClient.create(createArticleCommand()).article();

            articleClient.deleteBySlug(created.slug());

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.findBySlug(created.slug())
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        }

        @Test
        void should_throw403_when_deleteNotOwned() {
            auth.register().login();

            var created = articleClient.create(createArticleCommand()).article();

            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.deleteBySlug(created.slug())
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void should_returnCorrectArticleData_when_deleteNotExisting() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.deleteBySlug("not-existing")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_createArticleWithTags() {
            auth.register().login();

            var tag1 = "tag-" + UUID.randomUUID();
            var tag2 = "tag-" + UUID.randomUUID();

            var command = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of(tag1, tag2)
            ));

            var article = articleClient.create(command).article();

            assertThat(article.tagList()).containsExactlyInAnyOrder(tag1, tag2);

            assertThat(tagClient.findAll().tags()).contains(tag1, tag2);
        }

        @Test
        void should_createArticleWithExistingTags() {
            auth.register().login();

            // Create first article with tag1
            var command1 = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of("tag1")
            ));
            articleClient.create(command1);

            // Create second article with tag1 (existing) and tag2 (new)
            var command2 = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of("tag1", "tag2")
            ));

            var article = articleClient.create(command2).article();

            assertThat(article.tagList()).containsExactlyInAnyOrder("tag1", "tag2");
        }

        @Test
        void should_createArticleWithoutTags() {
            auth.register().login();

            var command = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    null
            ));

            var article = articleClient.create(command).article();

            assertThat(article.tagList()).isEmpty();
        }

        @Test
        void should_createArticleWithEmptyTags() {
            auth.register().login();

            var command = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of()
            ));

            var article = articleClient.create(command).article();

            assertThat(article.tagList()).isEmpty();
        }

        @Test
        void should_deleteArticleWithTags() {
            auth.register().login();

            var tags = List.of("tag-delete-1", "tag-delete-2");
            var command = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    tags
            ));

            var article = articleClient.create(command).article();

            articleClient.deleteBySlug(article.slug());

            // Verify deletion
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.findBySlug(article.slug())
            );
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_handleConcurrentArticleCreationWithTags() {
            auth.register().login();
            var tagName = "concurrent-tag-" + UUID.randomUUID();
            int count = 5;

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = IntStream.range(0, count)
                        .mapToObj(i -> executor.submit(() -> {
                            var command = new CreateArticle(new CreateArticle.Data(
                                    "title-" + i + "-" + UUID.randomUUID(),
                                    "desc",
                                    "body",
                                    List.of(tagName)
                            ));
                            return articleClient.create(command);
                        }))
                        .toList();

                futures.forEach(f -> {
                    try {
                        f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            assertThat(tagClient.findAll().tags()).contains(tagName);
        }

    }

    @Nested
    class ArticleFiltering {

        @Test
        void should_returnArticles_when_noFilters() {
            auth.register().login();
            var command1 = createArticleCommand();
            var command2 = createArticleCommand();
            articleClient.create(command1);
            articleClient.create(command2);

            var result = articleClient.findByFilters(null, null, null, 20, 0);

            assertThat(result.articles()).extracting(ArticleItemDto::title)
                    .contains(command1.article().title(), command2.article().title());
        }

        @Test
        void should_returnArticles_when_filterByTag() {
            auth.register().login();

            var tag = "tag-" + UUID.randomUUID();
            var commandWithTag = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of(tag)
            ));
            var commandWithoutTag = createArticleCommand();

            articleClient.create(commandWithTag);
            articleClient.create(commandWithoutTag);

            var result = articleClient.findByFilters(tag, null, null, 20, 0);

            assertThat(result.articles()).hasSize(1);
            assertThat(result.articles().get(0).title()).isEqualTo(commandWithTag.article().title());
        }

        @Test
        void should_returnArticles_when_filterByAuthor() {
            var author1 = auth.register().login().getUsername();
            var command1 = createArticleCommand();
            articleClient.create(command1);
            auth.logout();

            var author2 = auth.register().login().getUsername();
            var command2 = createArticleCommand();
            articleClient.create(command2);

            var result = articleClient.findByFilters(null, author1, null, 20, 0);

            assertThat(result.articles()).hasSize(1);
            assertThat(result.articles().get(0).author().username()).isEqualTo(author1);
            assertThat(result.articles().get(0).title()).isEqualTo(command1.article().title());
        }

        @Test
        void should_returnArticles_when_filterByFavorited() {
            var liker = auth.register().login().getUsername();
            auth.logout();

            var author = auth.register().login().getUsername();
            var command = createArticleCommand();
            var article = articleClient.create(command).article();
            auth.logout();

            auth.login(liker);
            articleClient.favorite(article.slug());

            var result = articleClient.findByFilters(null, null, liker, 20, 0);

            assertThat(result.articles()).hasSize(1);
            assertThat(result.articles().get(0).title()).isEqualTo(article.title());
        }

        @Test
        void should_returnArticles_when_pagination() {
            var user = auth.register().login().getUsername();

            var c1 = createArticleCommand();
            var c2 = createArticleCommand();
            var c3 = createArticleCommand();

            articleClient.create(c1);
            articleClient.create(c2);
            articleClient.create(c3);

            var page1 = articleClient.findByFilters(null, user, null, 2, 0);

            assertThat(page1.articles()).hasSize(2);
            assertThat(page1.articlesCount()).isEqualTo(3);
            assertThat(page1.articles()).extracting(ArticleItemDto::title)
                    .containsExactly(c3.article().title(), c2.article().title());

            var page2 = articleClient.findByFilters(null, user, null, 2, 2);
            assertThat(page2.articles()).hasSize(1);
            assertThat(page2.articlesCount()).isEqualTo(3);
            assertThat(page2.articles()).extracting(ArticleItemDto::title)
                    .containsExactly(c1.article().title());
        }

        @Test
        void should_returnEmpty_when_filterByNonExistingTag() {
            auth.register().login();
            articleClient.create(createArticleCommand());

            var result = articleClient.findByFilters("non-existing-tag", null, null, 20, 0);

            assertThat(result.articles()).isEmpty();
            assertThat(result.articlesCount()).isEqualTo(0);
        }

        @Test
        void should_returnEmpty_when_filterByNonExistingAuthor() {
            auth.register().login();
            articleClient.create(createArticleCommand());

            var result = articleClient.findByFilters(null, "non-existing-author", null, 20, 0);

            assertThat(result.articles()).isEmpty();
            assertThat(result.articlesCount()).isEqualTo(0);
        }

        @Test
        void should_returnEmpty_when_filterByNonExistingFavorited() {
            auth.register().login();
            articleClient.create(createArticleCommand());

            var result = articleClient.findByFilters(null, null, "non-existing-favorited", 20, 0);

            assertThat(result.articles()).isEmpty();
            assertThat(result.articlesCount()).isEqualTo(0);
        }

        @Test
        void should_returnArticles_when_filterByAllFilters() {
            var authorName = auth.register().login().getUsername();
            var tag = "shared-tag";
            var command = new CreateArticle(new CreateArticle.Data(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    List.of(tag)
            ));
            var article = articleClient.create(command).article();
            auth.logout();

            var likerName = auth.register().login().getUsername();
            articleClient.favorite(article.slug());

            var result = articleClient.findByFilters(tag, authorName, likerName, 20, 0);

            assertThat(result.articles()).hasSize(1);
            assertThat(result.articles().get(0).title()).isEqualTo(article.title());
            assertThat(result.articlesCount()).isEqualTo(1);
        }

    }

    @Nested
    class Favorites {

        @Test
        void should_returnCorrectArticleData_when_favoriteAndUnfavorite() {
            var user = auth.register().login().getUsername();

            var created = articleClient.create(createArticleCommand()).article();

            var favoritedArticle = articleClient.favorite(created.slug()).article();
            assertThat(favoritedArticle.favorited()).isTrue();
            assertThat(favoritedArticle.favoritesCount()).isEqualTo(1);

            var unfavoritedArticle = articleClient.unfavorite(created.slug()).article();
            assertThat(unfavoritedArticle.favorited()).isFalse();
            assertThat(unfavoritedArticle.favoritesCount()).isEqualTo(0);
        }

        @Test
        void should_throw404_when_favoriteNonExistentArticle() {
            auth.register().login();
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.favorite("non-existent-slug")
            );
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_throw404_when_unfavoriteNonExistentArticle() {
            auth.register().login();
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.unfavorite("non-existent-slug")
            );
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_handleDoubleFavorite() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();

            articleClient.favorite(created.slug());
            var secondFavorite = articleClient.favorite(created.slug()).article();

            assertThat(secondFavorite.favorited()).isTrue();
            assertThat(secondFavorite.favoritesCount()).isEqualTo(1);
        }

        @Test
        void should_handleDoubleUnfavorite() {
            auth.register().login();
            var created = articleClient.create(createArticleCommand()).article();
            articleClient.favorite(created.slug());

            articleClient.unfavorite(created.slug());
            var secondUnfavorite = articleClient.unfavorite(created.slug()).article();

            assertThat(secondUnfavorite.favorited()).isFalse();
            assertThat(secondUnfavorite.favoritesCount()).isEqualTo(0);
        }

    }

    @Nested
    class Comments {

        @Test
        void shouldReturnCorrectCommentData_whenCreateDeleteComment() {
            var user = auth.register().login().getUsername();

            var created = articleClient.create(createArticleCommand()).article();

            var addComment = new AddComment(null, new AddComment.Data(TEST_BODY));

            var comment = articleClient.addComment(created.slug(), addComment).comment();

            assertThat(comment.id()).isNotNull();
            assertThat(comment.author().username()).isEqualTo(user);
            assertThat(comment.body()).isEqualTo(TEST_BODY);
            assertThat(comment.createdAt()).isNotNull();
            assertThat(comment.updatedAt()).isNotNull();

            var comments = articleClient.findAllComments(created.slug()).comments();

            assertThat(comments).hasSize(1);

            articleClient.deleteComment(created.slug(), comment.id());

            comments = articleClient.findAllComments(created.slug()).comments();

            assertThat(comments).hasSize(0);
        }

        @Test
        void should_throw403_when_commentIsNotOwned() {
            auth.register().login();

            var article = articleClient.create(createArticleCommand()).article();

            var addComment = new AddComment(null, new AddComment.Data(TEST_BODY));
            var comment = articleClient.addComment(article.slug(), addComment).comment();

            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.deleteComment(article.slug(), comment.id())
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void should_throw404_when_addCommentArticleDoesNotExist() {
            auth.register().login();

            var addComment = new AddComment(null, new AddComment.Data(TEST_BODY));
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.addComment(UUID.randomUUID().toString(), addComment)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_throw404_when_deleteCommentArticleDoesNotExist() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.deleteComment(UUID.randomUUID().toString(), 9999999L)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_throw404_when_deleteCommentDoesNotExist() {
            auth.register().login();

            var article = articleClient.create(createArticleCommand()).article();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.deleteComment(article.slug(), 9999999L)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_throw404_when_findAllCommentsArticleDoesNotExist() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.findAllComments("non-existing-article")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @Nested
    class Feed {

        @Test
        void should_returnFeed_when_followingUser() {
            var authorName = auth.register().login().getUsername();
            var article = articleClient.create(createArticleCommand()).article();
            auth.logout();

            auth.register().login();
            profileClient.follow(authorName);

            var result = articleClient.feed(20, 0);

            assertThat(result.articles()).hasSize(1);
            assertThat(result.articles().get(0).title()).isEqualTo(article.title());
            assertThat(result.articlesCount()).isEqualTo(1);
        }

        @Test
        void should_notReturnFeed_when_notFollowingUser() {
            auth.register().login().getUsername();
            articleClient.create(createArticleCommand()).article();
            auth.logout();

            auth.register().login();

            var result = articleClient.feed(20, 0);

            assertThat(result.articles()).isEmpty();
            assertThat(result.articlesCount()).isEqualTo(0);
        }

        @Test
        void should_returnFeed_when_pagination() {
            var authorName = auth.register().login().getUsername();
            var a1 = articleClient.create(createArticleCommand()).article();
            var a2 = articleClient.create(createArticleCommand()).article();
            var a3 = articleClient.create(createArticleCommand()).article();
            auth.logout();

            auth.register().login();
            profileClient.follow(authorName);

            var page1 = articleClient.feed(2, 0);
            assertThat(page1.articles()).hasSize(2);
            assertThat(page1.articlesCount()).isEqualTo(3);
            assertThat(page1.articles()).extracting(ArticleItemDto::title)
                    .containsExactly(a3.title(), a2.title());

            var page2 = articleClient.feed(2, 2);
            assertThat(page2.articles()).hasSize(1);
            assertThat(page2.articlesCount()).isEqualTo(3);
            assertThat(page2.articles()).extracting(ArticleItemDto::title)
                    .containsExactly(a1.title());
        }

        @Test
        void should_throw401_when_gettingFeedWithoutLogin() {
            auth.logout();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> articleClient.feed(20, 0)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

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
