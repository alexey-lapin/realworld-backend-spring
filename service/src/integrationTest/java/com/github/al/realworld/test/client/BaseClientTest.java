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

import com.github.al.realworld.api.operation.ArticleClient;
import com.github.al.realworld.api.operation.ProfileClient;
import com.github.al.realworld.api.operation.TagClient;
import com.github.al.realworld.api.operation.UserClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.http.server.LocalTestWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseClientTest {

    @Configuration(proxyBeanMethods = false)
    public static class RestConfig {

        @Bean
        public RestClient testRestClient(RestClient.Builder restClientBuilder,
                                         WebApplicationContext applicationContext,
                                         @Value("${api.version}") String path) {
            var localTestWebServer = LocalTestWebServer.obtain(applicationContext).withPath(path);
            return restClientBuilder.uriBuilderFactory(localTestWebServer.uriBuilderFactory())
                    .requestInterceptor((request, body, execution) -> {
                        String token = AuthSupport.TokenHolder.token;
                        if (token != null) {
                            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Token " + token);
                        }
                        return execution.execute(request, body);
                    })
                    .build();
        }

        @Bean
        public HttpServiceProxyFactory httpServiceProxyFactory(RestClient testRestClient) {
            var adapter = RestClientAdapter.create(testRestClient);
            return HttpServiceProxyFactory.builderFor(adapter).build();
        }

        @Bean
        public ArticleClient articleClient(HttpServiceProxyFactory httpServiceProxyFactory) {
            return httpServiceProxyFactory.createClient(ArticleClient.class);
        }

        @Bean
        public ProfileClient profileClient(HttpServiceProxyFactory httpServiceProxyFactory) {
            return httpServiceProxyFactory.createClient(ProfileClient.class);
        }

        @Bean
        public TagClient tagClient(HttpServiceProxyFactory httpServiceProxyFactory) {
            return httpServiceProxyFactory.createClient(TagClient.class);
        }

        @Bean
        public UserClient userClient(HttpServiceProxyFactory httpServiceProxyFactory) {
            return httpServiceProxyFactory.createClient(UserClient.class);
        }

    }

}
