package com.github.al.realworld.application.query;

import com.github.al.realworld.api.query.GetArticle;
import com.github.al.realworld.api.query.GetArticleResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.application.ex.ResourceNotFoundException;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetArticleHandler implements QueryHandler<GetArticleResult, GetArticle> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public GetArticleResult handle(GetArticle query) {
        Article article = articleRepository.findBySlug(query.getSlug()).orElseThrow(ResourceNotFoundException::new);
        Profile profile = userRepository.findByUsername(query.getUsername()).map(User::getProfile).orElse(null);
        return new GetArticleResult(ArticleAssembler.assemble(article, profile));
    }
}
