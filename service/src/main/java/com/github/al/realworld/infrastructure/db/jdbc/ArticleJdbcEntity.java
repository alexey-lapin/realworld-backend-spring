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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Table(name = "TBL_ARTICLE")
public class ArticleJdbcEntity extends AbstractUuidIdEntity {

//    @Id
//    private UUID id;
    private String slug;
    private String title;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

//    @Lob
    private String body;

//    @OneToOne
    private UUID authorId;

//    @Singular
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "article_id")
//    private Set<Comment> comments;

//    @Singular
//    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    @JoinTable(name = "tbl_article_tags",
//            joinColumns = @JoinColumn(name = "article_id"),
//            inverseJoinColumns = @JoinColumn(name = "tag_id")
//    )
//    private Set<Tag> tags;

    @MappedCollection(idColumn = "ARTICLE_ID")
    private Set<ArticleTagRef> tags;

//    @Singular
//    @ManyToMany
//    @JoinTable(name = "tbl_article_favorites",
//            joinColumns = @JoinColumn(name = "article_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id")
//    )
//    private Set<User> favoredUsers;

    public Article toDomain() {
        return Article.builder()
                .id(getId())
                .slug(slug)
                .title(title)
                .description(description)
                .body(body)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .authorId(authorId)
                .tagIds(tags.stream().map(ArticleTagRef::getTagId).toList())
                .build();
    }

    public static ArticleJdbcEntity fromDomain(Article article) {
        ArticleJdbcEntity entity = ArticleJdbcEntity.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .authorId(article.getAuthorId())
                .tags(article.getTagIds().stream().map(ArticleTagRef::new).collect(Collectors.toSet()))
                .build();
        entity.setId(article.getId());
        return entity;
    }

}
