package com.github.al.realworld.infrastructure.converter;

import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.domain.model.ArticleAssembly;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArticleConverter implements Converter<ArticleAssembly, ArticleDto> {

    private final ConversionService conversionService;

    public ArticleConverter(@Lazy ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public ArticleDto convert(ArticleAssembly source) {
        return ArticleDto.builder()
                .slug(source.article().getSlug())
                .title(source.article().getTitle())
                .description(source.article().getDescription())
                .body(source.article().getBody())
                .createdAt(source.article().getCreatedAt())
                .updatedAt(source.article().getUpdatedAt())
                .favorited(source.favorited())
                .favoritesCount(source.favoritesCount())
//                .tagList(article.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList()))
                .author(conversionService.convert(source.author(), ProfileDto.class))
                .build();
    }

}
