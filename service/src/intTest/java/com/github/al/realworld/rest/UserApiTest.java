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

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.api.operation.UserClient;
import com.github.al.realworld.rest.auth.AuthSupport;
import com.github.al.realworld.rest.support.BaseRestTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class UserApiTest extends BaseRestTest {

    public static final String ALTERED_EMAIL = "altered-email@example.com";
    public static final String ALTERED_USERNAME = "altered-username";
    public static final String ALTERED_PASSWORD = "altered-password";
    public static final String ALTERED_BIO = "altered-bio";
    public static final String ALTERED_IMAGE = "altered-image";

    @Autowired
    private AuthSupport auth;

    @Autowired
    private UserClient userClient;

    @Test
    void should_returnCorrectData_whenRegisterUser() {
        RegisterUser command = registerCommand();

        UserDto user = userClient.register(command).getUser();

        assertThat(user.getUsername()).isEqualTo(command.getUsername());
        assertThat(user.getEmail()).isEqualTo(command.getEmail());
        assertThat(user.getToken()).isNotBlank();
    }

    @Test
    void should_returnCorrectData_whenLoginUser() {
        RegisterUser command = registerCommand();

        userClient.register(command);

        UserDto user = userClient.login(new LoginUser(command.getEmail(), command.getPassword())).getUser();

        assertThat(user.getToken()).isNotBlank();
    }

    @Test
    void should_returnCorrectData_whenLoginUserWithWrongPassword() {
        RegisterUser command = registerCommand();

        userClient.register(command);

        RestClientResponseException exception = catchThrowableOfType(
                () -> userClient.login(new LoginUser(command.getEmail(), UUID.randomUUID().toString())),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void should_return400_whenLoginUser() {
        String s = UUID.randomUUID().toString();
        RestClientResponseException exception = catchThrowableOfType(
                () -> userClient.login(new LoginUser(s + "@ex.com", s)),
                RestClientResponseException.class
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void should_returnCorrectData_whenUpdateUser() {
        auth.register().login();

        UpdateUser updateUser = UpdateUser.builder()
                .email(ALTERED_EMAIL)
                .username(ALTERED_USERNAME)
                .password(ALTERED_PASSWORD)
                .bio(ALTERED_BIO)
                .image(ALTERED_IMAGE)
                .build();

        UserDto user = userClient.update(updateUser).getUser();

        assertThat(user.getEmail()).isEqualTo(ALTERED_EMAIL);
        assertThat(user.getUsername()).isEqualTo(ALTERED_USERNAME);
        assertThat(user.getBio()).isEqualTo(ALTERED_BIO);
        assertThat(user.getImage()).isEqualTo(ALTERED_IMAGE);
    }

    @Test
    void should_throw400_whenUpdateUserWithExistingEmail() {
        AuthSupport.RegisteredUser registeredUser = auth.register();

        auth.register().login();

        UpdateUser updateUser = UpdateUser.builder()
                .email(registeredUser.getEmail())
                .build();

        RestClientResponseException exception = catchThrowableOfType(
                () -> userClient.update(updateUser),
                RestClientResponseException.class
        );

        assertThat(exception).isNotNull();
    }

    @Test
    void should_throw400_whenUpdateUserWithExistingName() {
        AuthSupport.RegisteredUser registeredUser = auth.register();

        auth.register().login();

        UpdateUser updateUser = UpdateUser.builder()
                .username(registeredUser.getUsername())
                .build();

        RestClientResponseException exception = catchThrowableOfType(
                () -> userClient.update(updateUser),
                RestClientResponseException.class
        );

        assertThat(exception).isNotNull();
    }

    private static RegisterUser registerCommand() {
        return RegisterUser.builder()
                .username(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString() + "@ex.com")
                .password(UUID.randomUUID().toString())
                .build();
    }

}
