package com.github.al.realworld.application.command;

import com.github.al.realworld.api.ArticleDto;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import com.github.al.realworld.domain.Tag;
import com.github.al.realworld.domain.TagRepository;
import com.github.al.realworld.service.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CreateArticleHandler implements CommandHandler<CreateArticleResult, CreateArticle> {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final Slugify slugify;

    @Transactional
    @Override
    public CreateArticleResult handle(CreateArticle command) {
        Optional<Article> articleByTitleOptional = articleRepository.findByTitle(command.getTitle());
        if (articleByTitleOptional.isPresent()) {
            throw new RuntimeException("article with title already exists");
        }

        Article article = Article.builder()
                .slug(slugify.makeSlug(command.getTitle()))
                .title(command.getTitle())
                .description(command.getDescription())
                .body(command.getBody())
                .createdAt(ZonedDateTime.now())
                .build();

        command.getTagList().stream()
                .map(t -> tagRepository.findByName(t).orElse(new Tag(t)))
                .forEach(article::addTag);

        Article savedArticle = articleRepository.save(article);

        return new CreateArticleResult(ArticleDto.builder()
                .slug(savedArticle.getSlug())
                .title(savedArticle.getTitle())
                .description(savedArticle.getDescription())
                .createdAt(savedArticle.getCreatedAt())
                .updatedAt(savedArticle.getUpdatedAt())
                .body(savedArticle.getBody())
                .favorited(false)
                .favoritesCount(savedArticle.getFavoritedProfiles().size())
                .build());
    }
}
