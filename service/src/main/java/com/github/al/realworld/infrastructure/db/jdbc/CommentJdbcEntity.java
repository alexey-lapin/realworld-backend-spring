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
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
@Getter
@Table(name = "TBL_COMMENT")
public class CommentJdbcEntity extends AbstractSequentialIdEntity {

    private UUID articleId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String body;
    private UUID authorId;

    public Comment toDomain() {
        return Comment.builder()
                .id(getId())
                .articleId(articleId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .body(body)
                .authorId(authorId)
                .build();
    }

    public static CommentJdbcEntity fromDomain(Comment comment) {
        CommentJdbcEntity entity = CommentJdbcEntity.builder()
                .articleId(comment.getArticleId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .body(comment.getBody())
                .authorId(comment.getAuthorId())
                .build();
        entity.setId(comment.getId());
        return entity;
    }

}
