package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.api.query.GetArticles;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GetArticlesHandler implements QueryHandler<GetArticlesResult, GetArticles> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public GetArticlesResult handle(GetArticles query) {
        List<Article> articles = articleRepository.findByFilters(query.getTag(), query.getAuthor(), query.getFavorited());
        User user = userRepository.findByUsername(query.getUsername()).orElseThrow(() -> new RuntimeException());

        List<ArticleDto> results = new ArrayList<>();

        articles.forEach(article -> {
            results.add(ArticleAssembler.assemble(article, user.getProfile()));
        });

        return new GetArticlesResult(results, results.size());
    }
}
