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
package com.github.al.realworld.rest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.al.realworld.api.operation.ArticleClient;
import com.github.al.realworld.api.operation.ProfileClient;
import com.github.al.realworld.api.operation.TagClient;
import com.github.al.realworld.api.operation.UserClient;
import com.github.al.realworld.rest.auth.AuthSupport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseRestTest {

    @Configuration(proxyBeanMethods = false)
    public static class RestConfig {

        @Bean
        public RestTemplate restTemplate(RestTemplateBuilder builder, Environment environment) {
            LocalHostUriTemplateHandler uriTemplateHandler =
                    new LocalHostUriTemplateHandler(environment) {
                        @Override
                        public String getRootUri() {
                            return super.getRootUri() + environment.getRequiredProperty("api.version");
                        }
                    };

            ObjectMapper mapper = new ObjectMapper()
                    .enable(SerializationFeature.WRAP_ROOT_VALUE)
                    .registerModule(new JavaTimeModule());

            return builder.uriTemplateHandler(uriTemplateHandler)
                    .messageConverters(new MappingJackson2HttpMessageConverter(mapper))
                    .interceptors((request, body, execution) -> {
                        String token = AuthSupport.TokenHolder.token;
                        if (token != null) {
                            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Token " + token);
                        }
                        return execution.execute(request, body);
                    })
                    .build();
        }

        @Bean
        public HttpServiceProxyFactory httpServiceProxyFactory(RestTemplate restTemplate) {
            RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
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
