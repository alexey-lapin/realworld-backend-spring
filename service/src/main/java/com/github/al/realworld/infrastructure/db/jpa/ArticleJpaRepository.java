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
package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.Article;
import com.github.al.realworld.infrastructure.db.jdbc.ArticleJdbcEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleJpaRepository extends CrudRepository<Article, UUID> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findByTitle(String title);

    @Query("SELECT DISTINCT article_ FROM Article article_ " +
//            "LEFT JOIN article_.tags t " +
            "LEFT JOIN article_.authorId p " +
//            "LEFT JOIN article_.favoredUsers f " +
            "WHERE " +
            "1 = 1"
//            "(:tag IS NULL OR t.name = :tag) AND " +
//            "(:author IS NULL OR p.username = :author) AND " +
//            "(:favorited IS NULL OR f.username = :favorited)"
    )
    List<Article> findByFilters(
//            @Param("tag") String tag,
//                                @Param("author") String author,
//                                @Param("favorited") String favorited,
                                Pageable pageable);

    @Query("SELECT article_ FROM Article article_ JOIN article_.authorId au "
//            + "WHERE au.id IN :followees"
    )
    List<Article> findByFollowees(List<UUID> followees, Pageable pageable);

}
