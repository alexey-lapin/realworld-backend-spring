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

import com.github.al.realworld.api.operation.ProfileClient;
import com.github.al.realworld.rest.auth.AuthSupport;
import com.github.al.realworld.rest.support.BaseRestTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ProfileApiTest extends BaseRestTest {

    @Autowired
    private AuthSupport auth;

    @Autowired
    private ProfileClient profileClient;

    @AfterEach
    void afterEach() {
        auth.logout();
    }

    @Nested
    class GetProfile {

        @Test
        void should_returnNull_when_userIsNotRegistered() {
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> profileClient.findByUsername("u3")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_returnCorrectData_when_userIsRegistered() {
            auth.register("u1", "u1@example.com", "1234");

            var profile = profileClient.findByUsername("u1").profile();

            assertThat(profile.username()).isEqualTo("u1");
        }

    }

    @Nested
    class FollowProfile {

        @Test
        void should_throw401_when_unauthorized() {
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> profileClient.follow("u2")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void should_returnCorrectProfileData_when_followAndUnfollow() {
            auth.register().login();
            var user2 = auth.register().getUsername();

            profileClient.follow(user2);

            var profile1 = profileClient.findByUsername(user2).profile();
            assertThat(profile1.following()).isTrue();

            profileClient.unfollow(user2);

            var profile2 = profileClient.findByUsername(user2).profile();
            assertThat(profile2.following()).isFalse();
        }

        @Test
        void should_throw404_when_followNonExistentUser() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> profileClient.follow("non-existent-user")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_handleDoubleFollow() {
            auth.register().login();
            var user2 = auth.register().getUsername();

            profileClient.follow(user2);
            var profile = profileClient.follow(user2).profile();

            assertThat(profile.following()).isTrue();
        }

    }

    @Nested
    class UnfollowProfile {

        @Test
        void should_throw404_when_unfollowNonExistentUser() {
            auth.register().login();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> profileClient.unfollow("non-existent-user")
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void should_handleDoubleUnfollow() {
            auth.register().login();
            var user2 = auth.register().getUsername();

            profileClient.follow(user2);
            profileClient.unfollow(user2);
            var profile = profileClient.unfollow(user2).profile();

            assertThat(profile.following()).isFalse();
        }

    }

}
