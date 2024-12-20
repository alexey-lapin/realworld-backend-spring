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

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentAssemblyJdbcRepository extends Repository<CommentAssemblyJdbcEntity, Long> {

    // language=SQL
    String BASE_QUERY = """
            select ca.*, \
                   case \
                       when :currentUserId is null then false \
                       else exists (select 1 \
                                    from "t_follows" fl \
                                    where fl."followee_id" = ca."author_id" \
                                      and fl."follower_id" = :currentUserId) \
                       end as author_following \
            from "v_comments_read" ca \
            """;

    @Query(BASE_QUERY + """
            where ca."id" = :id""")
    Optional<CommentAssemblyJdbcEntity> findById(@Param("id") long id,
                                                 @Param("currentUserId") Long currentUserId);

    @Query(BASE_QUERY + """
            where ca."article_id" = :articleId""")
    List<CommentAssemblyJdbcEntity> findAllByArticleId(@Param("articleId") long articleId,
                                                       @Param("currentUserId") Long currentUserId);

}
