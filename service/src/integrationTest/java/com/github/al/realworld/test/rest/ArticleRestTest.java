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
package com.github.al.realworld.test.rest;

import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.command.RegisterUserResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.http.server.LocalTestWebServer;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleRestTest {

    private final RestTestClient client;

    ArticleRestTest(WebApplicationContext applicationContext,
                    @Value("${api.version}") String apiVersion) {
        var localTestWebServer = LocalTestWebServer.obtain(applicationContext).withPath(apiVersion);
        this.client = RestTestClient.bindToServer()
                .uriBuilderFactory(localTestWebServer.uriBuilderFactory())
                .build();
    }

    @Test
    void should_createArticle() {
        var authorName = randomValue();
        var authorToken = registerUser(client, authorName, email(authorName), randomValue());

        createArticle(client, authorToken);
    }

    @Test
    void should_failToParseTokenAndReturn401() {
        var authorName = randomValue();
        registerUser(client, authorName, email(authorName), randomValue());

        client.post()
                .uri("/articles")
                .header(HttpHeaders.AUTHORIZATION, "Token " + randomValue())
                .body(new CreateArticle(new CreateArticle.Data(randomValue(), randomValue(), randomValue(), null)))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_failToParseHeaderAndReturn401() {
        var authorName = randomValue();
        registerUser(client, authorName, email(authorName), randomValue());

        client.post()
                .uri("/articles")
                .header(HttpHeaders.AUTHORIZATION, randomValue())
                .body(new CreateArticle(new CreateArticle.Data(randomValue(), randomValue(), randomValue(), null)))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    private String registerUser(RestTestClient client, String username, String email, String password) {
        return client.post()
                .uri("/users")
                .body(new RegisterUser(new RegisterUser.Data(email, username, password)))
                .exchangeSuccessfully()
                .expectBody(RegisterUserResult.class)
                .consumeWith(r -> {
                    assertThat(r.getResponseBody()).isNotNull();
                    assertThat(r.getResponseBody().user()).isNotNull();
                    assertThat(r.getResponseBody().user().token()).isNotNull();
                })
                .returnResult()
                .getResponseBody()
                .user()
                .token();
    }

    private String createArticle(RestTestClient client, String token) {
        return client.post()
                .uri("/articles")
                .header(HttpHeaders.AUTHORIZATION, "Token " + token)
                .body(new CreateArticle(new CreateArticle.Data(randomValue(), randomValue(), randomValue(), null)))
                .exchangeSuccessfully()
                .expectBody(CreateArticleResult.class)
                .consumeWith(r -> {
                    assertThat(r.getResponseBody()).isNotNull();
                    assertThat(r.getResponseBody().article()).isNotNull();
                    assertThat(r.getResponseBody().article().slug()).isNotNull();
                })
                .returnResult()
                .getResponseBody()
                .article()
                .slug();
    }

    private static String randomValue() {
        return UUID.randomUUID().toString();
    }

    private static String email(String username) {
        return username + "@ex.com";
    }

}
