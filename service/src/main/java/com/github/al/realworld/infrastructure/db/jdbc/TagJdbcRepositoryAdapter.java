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

import com.github.al.realworld.domain.model.Tag;
import com.github.al.realworld.domain.repository.TagRepository;
import com.github.al.realworld.infrastructure.config.MappingConfig;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class TagJdbcRepositoryAdapter implements TagRepository {

    private final TagJdbcRepository repository;
    private final TagMapper tagMapper;

    @Override
    public Optional<Tag> findByName(String name) {
        return repository.findByName(name)
                .map(tagMapper::toDomain);
    }

    @Override
    public List<Tag> findAllByNameIn(Collection<String> names) {
        return repository.findAllByNameIn(names).stream()
                .map(tagMapper::toDomain)
                .toList();
    }

    @Override
    public List<Tag> findAll() {
        return repository.findAll().stream()
                .map(tagMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Tag saveOrGet(Tag tag) {
        var entity = tagMapper.fromDomain(tag);
        try {
            var saved = repository.save(entity);
            return tagMapper.toDomain(saved);
        } catch (DbActionExecutionException ex) {
            if (ex.getCause() instanceof DuplicateKeyException) {
                return findByName(tag.name()).orElseThrow();
            }
            throw ex;
        } catch (DuplicateKeyException ex) {
            return findByName(tag.name()).orElseThrow();
        }
    }

    @AnnotateWith(MappingConfig.GeneratedMapper.class)
    @Mapper(config = MappingConfig.class)
    interface TagMapper {

        Tag toDomain(TagJdbcEntity source);

        TagJdbcEntity fromDomain(Tag source);

    }

}
