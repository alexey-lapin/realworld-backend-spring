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
package com.github.al.realworld.infrastructure.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.startup.StartupEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers("/h2-console", "/h2-console/**").permitAll())
                .headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(configurer -> {
                    configurer.requestMatchers(EndpointRequest.to(
                                    HealthEndpoint.class,
                                    InfoEndpoint.class,
                                    PrometheusScrapeEndpoint.class,
                                    StartupEndpoint.class))
                            .permitAll();
                    configurer.requestMatchers("/error").permitAll();
                    configurer.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    configurer.requestMatchers(HttpMethod.GET, "/api/articles/feed").authenticated();
                    configurer.requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll();
                    configurer.requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll();
                    configurer.requestMatchers(HttpMethod.GET, "/api/profiles/**").permitAll();
                    configurer.requestMatchers(HttpMethod.GET, "/api/tags").permitAll();
                    configurer.requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    configurer.anyRequest().authenticated();
                });
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RSAKey rsaKey() throws Exception {
        return new RSAKeyGenerator(2048).generate();
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtEncoder.withKeyPair(rsaKey.toRSAPublicKey(), rsaKey.toRSAPrivateKey()).build();
    }

    /**
     * This is usually not necessary and oauth2-resource-server config works out-of-the-box,
     * but we need to support "Token" scheme in addition to "Bearer" scheme.
     *
     * @see org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new BearerTokenResolver() {
            private static final Pattern pattern = Pattern.compile("^(Bearer|Token) (?<token>[a-zA-Z0-9-._~+/]+=*)$",
                    Pattern.CASE_INSENSITIVE);

            @Override
            public @Nullable String resolve(HttpServletRequest request) {
                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization == null) {
                    return null;
                }
                Matcher matcher = pattern.matcher(authorization);
                if (!matcher.matches()) {
                    BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
                    throw new OAuth2AuthenticationException(error);
                }
                return matcher.group("token");
            }
        };
    }

}
