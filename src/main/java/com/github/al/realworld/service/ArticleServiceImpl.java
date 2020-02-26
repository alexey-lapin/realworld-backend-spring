package com.github.al.realworld.service;

import com.github.al.realworld.application.command.ArticleCreateCommand;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.ArticleRepository;
import com.github.al.realworld.domain.Tag;
import com.github.al.realworld.domain.TagRepository;
import com.github.al.realworld.infrastructure.web.dto.ArticleDto;
import com.github.al.realworld.infrastructure.web.dto.ArticlesDto;
import com.github.al.realworld.infrastructure.web.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final Slugify slugify;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public ArticleDto create(ArticleCreateCommand cmd) {
        Optional<Article> articleByTitleOptional = articleRepository.findByTitle(cmd.getTitle());
        if (articleByTitleOptional.isPresent()) {
            throw new RuntimeException("article with title already exists");
        }

        Article article = Article.builder()
                .slug(slugify.makeSlug(cmd.getTitle()))
                .title(cmd.getTitle())
                .description(cmd.getDescription())
                .body(cmd.getBody())
                .createdAt(LocalDate.now())
                .build();

        cmd.getTagList().stream()
                .map(t -> tagRepository.findByName(t).orElse(new Tag(t)))
                .forEach(article::addTag);

        Article savedArticle = articleRepository.save(article);

        return articleMapper.toDto(savedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticlesDto findAll() {
        return articleMapper.toListDto(articleRepository.findAll());
    }

    @Override
    public void deleteBySlug(String slug) {

    }
}
