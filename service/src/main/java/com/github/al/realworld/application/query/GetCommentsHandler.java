package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.api.query.GetComments;
import com.github.al.realworld.api.query.GetCommentsResult;
import com.github.al.realworld.application.CommentAssembler;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Article;
import com.github.al.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class GetCommentsHandler implements QueryHandler<GetCommentsResult, GetComments> {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    @Override
    public GetCommentsResult handle(GetComments query) {
        Article article = articleRepository.findBySlug(query.getSlug()).orElseThrow(() -> new RuntimeException());

        ArrayList<CommentDto> result = new ArrayList<>();

        article.getComments().forEach(comment -> result.add(CommentAssembler.assemble(comment)));

        return new GetCommentsResult(result);
    }
}
