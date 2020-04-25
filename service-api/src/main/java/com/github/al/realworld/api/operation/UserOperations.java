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
package com.github.al.realworld.api.operation;

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.LoginUserResult;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.command.RegisterUserResult;
import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.command.UpdateUserResult;
import com.github.al.realworld.api.query.GetCurrentUserResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface UserOperations {

    @PostMapping("/users/login")
    LoginUserResult login(@Valid @RequestBody LoginUser command);

    @PostMapping("/users")
    RegisterUserResult register(@Valid @RequestBody RegisterUser command);

    @GetMapping("/user")
    GetCurrentUserResult current();

    @PutMapping("/user")
    UpdateUserResult update(@Valid @RequestBody UpdateUser command);

}
