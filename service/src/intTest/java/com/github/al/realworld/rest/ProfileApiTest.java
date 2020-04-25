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

import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.api.operation.ProfileClient;
import com.github.al.realworld.rest.auth.AuthSupport;
import com.github.al.realworld.rest.support.FeignBasedRestTest;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ProfileApiTest extends FeignBasedRestTest {

    @Autowired
    private AuthSupport auth;

    @Autowired
    private ProfileClient profileClient;

    @AfterEach
    void afterEach() {
        auth.logout();
    }

    @Test
    void should_returnNull_when_userIsNotRegistered() {

        FeignException exception = catchThrowableOfType(
                () -> profileClient.findByUsername("u3"),
                FeignException.class
        );

        assertThat(exception.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void should_returnCorrectData_when_userIsRegistered() {
        auth.register("u1", "u1@example.com", "1234");

        ProfileDto profile = profileClient.findByUsername("u1").getProfile();

        assertThat(profile.getUsername()).isEqualTo("u1");
    }

    @Test
    void should_throw401_when_unauthorized() {
        FeignException ex = catchThrowableOfType(
                () -> profileClient.follow("u2"),
                FeignException.class
        );

        assertThat(ex.status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void should_returnCorrectProfileData_when_followAndUnfollow() {
        auth.register().login();
        String user2 = auth.register().getUsername();

        profileClient.follow(user2);

        ProfileDto profile1 = profileClient.findByUsername(user2).getProfile();
        assertThat(profile1.getFollowing()).isTrue();

        profileClient.unfollow(user2);

        ProfileDto profile2 = profileClient.findByUsername(user2).getProfile();
        assertThat(profile2.getFollowing()).isFalse();
    }

}
