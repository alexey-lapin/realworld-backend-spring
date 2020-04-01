package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.api.query.GetComments;
import com.github.al.realworld.api.query.GetCommentsResult;
import com.github.al.realworld.application.CommentAssembler;
import com.github.al.realworld.application.exception.ResourceNotFoundException;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ArticleRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static com.github.al.realworld.application.exception.ResourceNotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class GetCommentsHandler implements QueryHandler<GetCommentsResult, GetComments> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public GetCommentsResult handle(GetComments query) {
        Article article = articleRepository.findBySlug(query.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exists", query.getSlug()));

        Profile currentProfile = userRepository.findByUsername(query.getCurrentUsername())
                .map(User::getProfile)
                .orElse(null);

        ArrayList<CommentDto> result = new ArrayList<>();

        article.getComments().forEach(comment -> result.add(CommentAssembler.assemble(comment, currentProfile)));

        return new GetCommentsResult(result);
    }
}