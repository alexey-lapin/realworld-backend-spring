package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.application.service.Slugify;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.Tag;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.TagRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CreateArticleHandler implements CommandHandler<CreateArticleResult, CreateArticle> {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final Slugify slugify;

    @Transactional
    @Override
    public CreateArticleResult handle(CreateArticle command) {
        Optional<Article> articleByTitleOptional = articleRepository.findByTitle(command.getTitle());
        if (articleByTitleOptional.isPresent()) {
            throw new RuntimeException("article with title already exists");
        }

        ZonedDateTime now = ZonedDateTime.now();
        User user = userRepository.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException());
        Profile profile = user.getProfile();

        Article article = Article.builder()
                .id(UUID.randomUUID())
                .slug(slugify.makeSlug(command.getTitle()))
                .title(command.getTitle())
                .description(command.getDescription())
                .body(command.getBody())
                .createdAt(now)
                .updatedAt(now)
                .author(profile)
                .build();

        command.getTagList().stream()
                .map(t -> tagRepository.findByName(t).orElse(new Tag(t)))
                .forEach(article::addTag);

        Article savedArticle = articleRepository.save(article);

        return new CreateArticleResult(ArticleAssembler.assemble(savedArticle, profile));
    }
}
