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
import com.github.al.realworld.domain.model.ArticleAssembly;
import com.github.al.realworld.domain.model.ArticleItem;
import com.github.al.realworld.domain.model.Profile;
import com.github.al.realworld.domain.model.Tag;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.infrastructure.config.mapping.MappingConfig;
import com.github.al.realworld.infrastructure.mapping.GeneratedMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ArticleJdbcRepositoryAdapter implements ArticleRepository {

    private final ArticleJdbcRepository repository;
    private final ArticleAssemblyJdbcRepository assemblyRepository;
    private final ArticleMapper articleMapper;

    @Override
    public Optional<Long> findIdBySlug(String slug) {
        return repository.findIdBySlug(slug);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(articleMapper::toDomain);
    }

    @Override
    public void incrementFavoriteCount(long id) {
        repository.incrementFavoriteCount(id);
    }

    @Override
    public void decrementFavoriteCount(long id) {
        repository.decrementFavoriteCount(id);
    }

    @Override
    public Article save(Article article) {
        var entity = articleMapper.fromDomain(article);
        var saved = repository.save(entity);
        return articleMapper.toDomain(saved);
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<ArticleAssembly> findAssemblyById(@Nullable Long userId, long id) {
        return assemblyRepository.findById(id, userId)
                .map(articleMapper::toDomain);
    }

    @Override
    public Optional<ArticleAssembly> findAssemblyBySlug(@Nullable Long userId, String slug) {
        return assemblyRepository.findBySlug(slug, userId)
                .map(articleMapper::toDomain);
    }

    @Override
    public List<ArticleItem> findAllItemsByFilters(@Nullable Long userId,
                                                   @Nullable Long tagId,
                                                   @Nullable Long authorId,
                                                   @Nullable Long favoritedById,
                                                   int limit,
                                                   long offset) {
        return assemblyRepository.findFiltered(tagId, authorId, favoritedById, userId, limit, offset).stream()
                .map(articleMapper::toItem)
                .toList();
    }

    @Override
    public List<ArticleItem> findAllItemsByFollowerId(long userId, int limit, long offset) {
        return assemblyRepository.findFeed(userId, limit, offset).stream()
                .map(articleMapper::toItem)
                .toList();
    }

    @Override
    public long countByFilters(@Nullable Long tagId, @Nullable Long authorId, @Nullable Long favoritedById) {
        return assemblyRepository.countFiltered(tagId, authorId, favoritedById);
    }

    @Override
    public long countFeed(long userId) {
        return assemblyRepository.countFeed(userId);
    }

    @AnnotateWith(GeneratedMapper.class)
    @Mapper(config = MappingConfig.class)
    public interface ArticleMapper {

        @Mapping(target = "tagList", source = "tags")
        @Mapping(target = "tagIds", source = "tags")
        ArticleJdbcEntity fromDomain(Article source);

        @Mapping(target = "tag", ignore = true)
        @Mapping(target = "tags", source = "source")
        Article toDomain(ArticleJdbcEntity source);

        @Mapping(target = "author", source = "source")
        ArticleAssembly toDomain(ArticleAssemblyJdbcEntity source);

        @Mapping(target = "author", source = "source")
        ArticleItem toItem(ArticleAssemblyJdbcEntity source);

        default Profile profile(ArticleAssemblyJdbcEntity source) {
            return new Profile(source.authorUsername(),
                    source.authorBio(),
                    source.authorImage(),
                    source.authorFollowing());
        }

        default @Nullable List<String> tagList(@Nullable String source) {
            if (source == null) {
                return null;
            }
            return Arrays.stream(source.split(",")).toList();
        }

        default @Nullable String tagList(@Nullable List<Tag> source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            return source.stream().map(Tag::name).collect(Collectors.joining(","));
        }

        default List<ArticleTagJdbcEntity> tagIds(List<Tag> source) {
            return source.stream().map(t -> new ArticleTagJdbcEntity(t.id())).toList();
        }

        default List<Tag> tags(ArticleJdbcEntity source) {
            var tagIds = source.tagIds();
            if (tagIds != null) {
                var tagList = source.tagList();
                if (tagList != null) {
                    var tagNames = tagList.split(",");
                    if (tagNames.length == tagIds.size()) {
                        List<Tag> tags = new ArrayList<>();
                        for (var i = 0; i < tagIds.size(); i++) {
                            var tag = new Tag(tagIds.get(i).tagId(), tagNames[i]);
                            tags.add(tag);
                        }
                        return tags;
                    }
                }
            }
            return List.of();
        }

    }

}
