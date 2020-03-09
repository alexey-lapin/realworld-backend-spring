package com.github.al.realworld.application.query;

import com.github.al.realworld.api.query.GetComment;
import com.github.al.realworld.api.query.GetCommentResult;
import com.github.al.realworld.application.CommentAssembler;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.Comment;
import com.github.al.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetCommentHandler implements QueryHandler<GetCommentResult, GetComment> {

    private final ArticleRepository articleRepository;

    @Transactional
    @Override
    public GetCommentResult handle(GetComment query) {
        Article article = articleRepository.findBySlug(query.getSlug()).orElseThrow(() -> new RuntimeException());

        Comment comment = article.getComments().stream()
                .filter(c -> c.getId().equals(query.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException());

        return new GetCommentResult(CommentAssembler.assemble(comment));
    }
}
