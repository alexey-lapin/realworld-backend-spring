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

import com.github.al.realworld.domain.model.Comment;
import com.github.al.realworld.domain.model.CommentAssembly;
import com.github.al.realworld.domain.model.Profile;
import com.github.al.realworld.domain.repository.CommentRepository;
import com.github.al.realworld.infrastructure.config.MappingConfig;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CommentJdbcRepositoryAdapter implements CommentRepository {

    private final CommentJdbcRepository repository;
    private final CommentAssemblyJdbcRepository assemblyRepository;
    private final CommentMapper commentMapper;

    @Override
    public Optional<Comment> findById(long id) {
        return repository.findById(id)
                .map(commentMapper::toDomain);
    }

    @Override
    public Optional<CommentAssembly> findAssemblyById(Long userId, long id) {
        return assemblyRepository.findById(id, userId)
                .map(commentMapper::toDomain);
    }

    @Override
    public List<CommentAssembly> findAllAssemblyByArticleId(Long userId, long articleId) {
        return assemblyRepository.findAllByArticleId(articleId, userId).stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public Comment save(Comment comment) {
        var entity = commentMapper.fromDomain(comment);
        var saved = repository.save(entity);
        return commentMapper.toDomain(saved);
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByArticleId(long articleId) {
        repository.deleteAllByArticleId(articleId);
    }

    @AnnotateWith(MappingConfig.GeneratedMapper.class)
    @Mapper(config = MappingConfig.class)
    interface CommentMapper {

        Comment toDomain(CommentJdbcEntity source);

        CommentJdbcEntity fromDomain(Comment source);

        @Mapping(target = "author", source = "source")
        CommentAssembly toDomain(CommentAssemblyJdbcEntity source);

        default Profile profile(CommentAssemblyJdbcEntity source) {
            return new Profile(source.authorUsername(),
                    source.authorBio(),
                    source.authorImage(),
                    source.authorFollowing());
        }

    }

}
