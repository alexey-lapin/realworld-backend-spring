package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.ArticleDto;
import com.github.al.realworld.api.query.GetFeed;
import com.github.al.realworld.api.query.GetFeedResult;
import com.github.al.realworld.application.ArticleAssembler;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.*;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.FollowRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GetFeedHandler implements QueryHandler<GetFeedResult, GetFeed> {

    private final ArticleRepository articleRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public GetFeedResult handle(GetFeed query) {
        List<Follow> followees = followRepository.findByFollower(query.getUsername());
        List<Article> articles = articleRepository.findByFollowees(followees.stream().map(Follow::getFollowee).collect(Collectors.toList()));
        User user = userRepository.findByUsername(query.getUsername()).orElseThrow(() -> new RuntimeException());

        ArrayList<ArticleDto> results = new ArrayList<>();

        articles.forEach(article -> {
            results.add(ArticleAssembler.assemble(article, user.getProfile()));
        });

        return new GetFeedResult(results, results.size());
    }
}
