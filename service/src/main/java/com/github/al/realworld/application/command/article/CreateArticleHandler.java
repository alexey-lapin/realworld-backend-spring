package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.application.service.SlugService;
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

import static com.github.al.realworld.application.exception.InvalidRequestException.invalidRequest;

@RequiredArgsConstructor
@Service
public class CreateArticleHandler implements CommandHandler<CreateArticleResult, CreateArticle> {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final SlugService slugService;

    @Transactional
    @Override
    public CreateArticleResult handle(CreateArticle command) {
        Optional<Article> articleByTitleOptional = articleRepository.findByTitle(command.getTitle());
        if (articleByTitleOptional.isPresent()) {
            throw invalidRequest("article [title=%s] already exists", command.getTitle());
        }

        Profile currentProfile = userRepository.findByUsername(command.getUsername())
                .map(User::getProfile)
                .orElseThrow(() -> invalidRequest("user [name=%s] does not exist", command.getUsername()));

        ZonedDateTime now = ZonedDateTime.now();

        Article.ArticleBuilder articleBuilder = Article.builder()
                .id(UUID.randomUUID())
                .slug(slugService.makeSlug(command.getTitle()))
                .title(command.getTitle())
                .description(command.getDescription())
                .body(command.getBody())
                .createdAt(now)
                .updatedAt(now)
                .author(currentProfile);

        command.getTagList().stream()
                .map(t -> tagRepository.findByName(t).orElseGet(() -> new Tag(t)))
                .forEach(articleBuilder::tag);

        Article savedArticle = articleRepository.save(articleBuilder.build());

        return new CreateArticleResult(ArticleAssembler.assemble(savedArticle, currentProfile));
    }
}
