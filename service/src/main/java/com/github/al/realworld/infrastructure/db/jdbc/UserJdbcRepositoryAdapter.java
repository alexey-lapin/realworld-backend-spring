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
package com.github.al.realworld.infrastructure.db.jdbc;

import com.github.al.realworld.domain.model.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.infrastructure.config.MappingConfig;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserJdbcRepositoryAdapter implements UserRepository {

    private final ConversionService conversionService;
    private final UserJdbcRepository repository;

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public Optional<User> findById(long id) {
        return repository.findById(id)
                .map(e -> conversionService.convert(e, User.class));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(e -> conversionService.convert(e, User.class));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(e -> conversionService.convert(e, User.class));
    }

    @Override
    public User save(User user) {
        var entity = conversionService.convert(user, UserJdbcEntity.class);
        var saved = repository.save(entity);
        return conversionService.convert(saved, User.class);
    }

    @AnnotateWith(MappingConfig.GeneratedMapper.class)
    @Mapper(config = MappingConfig.class)
    interface ToDomainUserMapper extends Converter<UserJdbcEntity, User> {

        @Override
        User convert(UserJdbcEntity source);

    }

    @AnnotateWith(MappingConfig.GeneratedMapper.class)
    @Mapper(config = MappingConfig.class)
    interface FromDomainUserMapper extends Converter<User, UserJdbcEntity> {

        @Override
        UserJdbcEntity convert(User source);

    }

}
