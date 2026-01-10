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
package com.github.al.realworld.rest.auth;

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.LoginUserResult;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.operation.UserClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthSupport {

    @Autowired(required = false)
    private UserClient userClient;

    public RegisteredUser register() {
        String uuid = UUID.randomUUID().toString();
        return register(uuid, email(uuid), uuid);
    }

    public RegisteredUser register(String username, String email, String password) {
        userClient.register(new RegisterUser(new RegisterUser.Data(email, username, password)));
        return new RegisteredUser(email, username, password);
    }

    private String email(String uuid) {
        return uuid + "@ex.com";
    }

    public void login(String cred) {
        login(email(cred), cred);
    }

    public void login(String email, String password) {
        LoginUserResult result = userClient.login(new LoginUser(new LoginUser.Data(email, password)));
        TokenHolder.token = result.user().token();
    }

    public void logout() {
        TokenHolder.token = null;
    }

    public static class TokenHolder {
        public static String token;
    }

    @AllArgsConstructor
    @Getter
    public class RegisteredUser {
        private String email;
        private String username;
        private String password;

        public RegisteredUser login() {
            AuthSupport.this.login(email, password);
            return this;
        }
    }

}
