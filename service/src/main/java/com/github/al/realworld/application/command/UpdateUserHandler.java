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
package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.command.UpdateUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.application.service.JwtService;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.model.UserWithToken;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.BadRequestException.badRequest;
import static com.github.al.realworld.application.exception.NotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class UpdateUserHandler implements CommandHandler<UpdateUserResult, UpdateUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final ConversionService conversionService;

    @Transactional
    @Override
    public UpdateUserResult handle(UpdateUser command) {
        var user = userRepository.findByUsername(command.getCurrentUsername())
                .orElseThrow(() -> notFound("user [name=%s] does not exist", command.getCurrentUsername()));

        if (command.getUsername() != null
            && !command.getUsername().equals(user.username())
            && userRepository.existsByUsername(command.getUsername())) {
            throw badRequest("user [name=%s] already exists", command.getUsername());
        }

        if (command.getEmail() != null
            && !command.getEmail().equals(user.email())
            && userRepository.existsByEmail(command.getEmail())) {
            throw badRequest("user [email=%s] already exists", command.getEmail());
        }

        var alteredUser = user.toBuilder()
                .email(command.getEmail() == null ? user.email() : command.getEmail())
                .username(command.getUsername() == null ? user.username() : command.getUsername())
                .password(command.getPassword() == null ? user.password() : encoder.encode(command.getPassword()))
                .bio(command.getBio() == null ? user.bio() : command.getBio())
                .image(command.getImage() == null ? user.image() : command.getImage())
                .build();

        var savedUser = userRepository.save(alteredUser);

        var token = jwtService.getToken(savedUser);
        var data = conversionService.convert(new UserWithToken(savedUser, token), UserDto.class);

        return new UpdateUserResult(data);
    }

}
