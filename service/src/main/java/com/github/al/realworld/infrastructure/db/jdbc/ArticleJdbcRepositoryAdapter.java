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

import com.github.al.realworld.domain.model.Article;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.infrastructure.db.jpa.ArticleJpaRepository;
import com.github.al.realworld.infrastructure.db.jpa.OffsetBasedPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class ArticleJdbcRepositoryAdapter implements ArticleRepository {

    private final ArticleJdbcRepository repository;

    @Override
    public Optional<UUID> findIdBySlug(String slug) {
        return repository.findIdBySlug(slug);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return repository.findBySlug(slug).map(ArticleJdbcEntity::toDomain);
    }

    @Override
    public Optional<Article> findByTitle(String title) {
        return repository.findByTitle(title).map(ArticleJdbcEntity::toDomain);
    }

    @Override
    public List<Article> findByFilters(String tag, String author, String favorited, Integer limit, Integer offset) {
        Pageable pageable = new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Order.desc("createdAt")));
        return repository.findByFilters(/*tag,*/ /*author, favorited,*/ pageable).stream()
                .map(ArticleJdbcEntity::toDomain).toList();
    }

    @Override
    public List<Article> findByFolloweeIds(List<UUID> followeeIds, Integer limit, Integer offset) {
        Pageable pageable = new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Order.desc("createdAt")));
        return repository.findByFollowees(followeeIds, pageable).stream()
                .map(ArticleJdbcEntity::toDomain).toList();
    }

    @Override
    public void delete(Article article) {
        repository.delete(ArticleJdbcEntity.fromDomain(article));
    }

    @Override
    public Article save(Article article) {
        return repository.save(ArticleJdbcEntity.fromDomain(article)).toDomain();
    }

}
