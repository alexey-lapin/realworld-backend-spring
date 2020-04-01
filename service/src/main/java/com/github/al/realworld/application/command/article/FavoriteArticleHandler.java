package com.github.al.realworld.application.command.article;

import com.github.al.realworld.api.command.FavoriteArticle;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.al.realworld.application.exception.InvalidRequestException.invalidRequest;
import static com.github.al.realworld.application.exception.ResourceNotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class FavoriteArticleHandler implements CommandHandler<FavoriteArticleResult, FavoriteArticle> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public FavoriteArticleResult handle(FavoriteArticle command) {
        Article article = articleRepository.findBySlug(command.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exist", command.getSlug()));

        Profile currentProfile = userRepository.findByUsername(command.getUsername())
                .map(User::getProfile)
                .orElseThrow(() -> invalidRequest("user [name=%s] does not exist", command.getUsername()));

        Article alteredArticle = article.toBuilder()
                .favoritedProfile(currentProfile)
                .build();

        Article savedArticle = articleRepository.save(alteredArticle);

        return new FavoriteArticleResult(ArticleAssembler.assemble(savedArticle, currentProfile));
    }
}
