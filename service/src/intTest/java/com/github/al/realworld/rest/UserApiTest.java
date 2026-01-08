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
import com.github.al.realworld.api.operation.UserClient;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.infrastructure.db.jdbc.UserJdbcRepository;
import com.github.al.realworld.rest.auth.AuthSupport;
import com.github.al.realworld.rest.support.BaseRestTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserJdbcRepository userJdbcRepository;

    @AfterEach
    void afterEach() {
        auth.logout();
    }

    @Nested
    class Registration {

        @Test
        void should_returnCorrectData_whenRegisterUser() {
            var command = registerCommand();

            var user = userClient.register(command).user();

            assertThat(user.username()).isEqualTo(command.user().username());
            assertThat(user.email()).isEqualTo(command.user().email());
            assertThat(user.token()).isNotBlank();
        }

        @Test
        void should_throw400_whenRegisterWithExistingEmail() {
            var command = registerCommand();
            userClient.register(command);

            var duplicateEmailCommand = new RegisterUser(new RegisterUser.Data(
                    command.user().email(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            ));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.register(duplicateEmailCommand)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void should_throw400_whenRegisterWithExistingUsername() {
            var command = registerCommand();
            userClient.register(command);

            var duplicateUsernameCommand = new RegisterUser(new RegisterUser.Data(
                    UUID.randomUUID() + "@ex.com",
                    command.user().username(),
                    UUID.randomUUID().toString()
            ));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.register(duplicateUsernameCommand)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class Login {

        @Test
        void should_returnCorrectData_whenLoginUser() {
            var command = registerCommand();
            userClient.register(command);

            var user = userClient.login(new LoginUser(new LoginUser.Data(command.user().email(), command.user().password()))).user();

            assertThat(user.token()).isNotBlank();
        }

        @Test
        void should_returnCorrectData_whenLoginUserWithWrongPassword() {
            var command = registerCommand();
            userClient.register(command);

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.login(new LoginUser(new LoginUser.Data(command.user().email(), UUID.randomUUID().toString())))
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void should_return400_whenLoginNonExistentUser() {
            var s = UUID.randomUUID().toString();
            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.login(new LoginUser(new LoginUser.Data(s + "@ex.com", s)))
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class GetCurrentUser {

        @Test
        void should_returnCorrectData_whenGetCurrentUser() {
            var registeredUser = auth.register().login();

            var user = userClient.current().user();

            assertThat(user.email()).isEqualTo(registeredUser.getEmail());
            assertThat(user.username()).isEqualTo(registeredUser.getUsername());
            assertThat(user.token()).isNotBlank();
        }

        @Test
        void should_throw401_whenGetCurrentUserWithoutLogin() {
            auth.logout();

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.current()
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void should_throw400_whenGetCurrentUser_and_userDoesNotExist() {
            var registeredUser = auth.register().login();

            // Delete the user but keep the token active
            userRepository.findByEmail(registeredUser.getEmail())
                    .ifPresent(user -> userJdbcRepository.deleteById(user.id()));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.current()
            );

            // GetCurrentUserHandler throws BadRequestException
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class UpdateUserTests {

        @Test
        void should_returnCorrectData_whenUpdateUser() {
            auth.register().login();

            var updateUser = new UpdateUser(new UpdateUser.Data(
                    ALTERED_EMAIL,
                    ALTERED_USERNAME,
                    ALTERED_PASSWORD,
                    ALTERED_IMAGE,
                    ALTERED_BIO
            ));

            var user = userClient.update(updateUser).user();

            assertThat(user.email()).isEqualTo(ALTERED_EMAIL);
            assertThat(user.username()).isEqualTo(ALTERED_USERNAME);
            assertThat(user.bio()).isEqualTo(ALTERED_BIO);
            assertThat(user.image()).isEqualTo(ALTERED_IMAGE);
        }

        @Test
        void should_throw400_whenUpdateUserWithExistingEmail() {
            var registeredUser = auth.register();

            auth.register().login();

            var updateUser = new UpdateUser(new UpdateUser.Data(
                    registeredUser.getEmail(),
                    null,
                    null,
                    null,
                    null
            ));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.update(updateUser)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void should_throw400_whenUpdateUserWithExistingName() {
            var registeredUser = auth.register();

            auth.register().login();

            var updateUser = new UpdateUser(new UpdateUser.Data(
                    null,
                    registeredUser.getUsername(),
                    null,
                    null,
                    null
            ));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.update(updateUser)
            );

            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void should_updateOnlyBio() {
            auth.register().login();

            var updateUser = new UpdateUser(new UpdateUser.Data(null, null, null, null, ALTERED_BIO));
            var user = userClient.update(updateUser).user();

            assertThat(user.bio()).isEqualTo(ALTERED_BIO);
        }

        @Test
        void should_throw404_whenUpdateUser_and_userDoesNotExist() {
            var registeredUser = auth.register().login();

            // Delete the user but keep the token active
            userRepository.findByEmail(registeredUser.getEmail())
                    .ifPresent(user -> userJdbcRepository.deleteById(user.id()));

            var updateUser = new UpdateUser(new UpdateUser.Data(
                    null, null, null, null, ALTERED_BIO
            ));

            var exception = catchThrowableOfType(
                    RestClientResponseException.class,
                    () -> userClient.update(updateUser)
            );

            // UpdateUserHandler throws NotFoundException
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    private static RegisterUser registerCommand() {
        return new RegisterUser(new RegisterUser.Data(
                UUID.randomUUID() + "@ex.com",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        ));
    }

}
