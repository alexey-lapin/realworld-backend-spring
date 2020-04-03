package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.command.DeleteArticle;
import com.github.al.realworld.api.command.DeleteComment;
import com.github.al.realworld.api.command.FavoriteArticle;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.api.command.UnfavoriteArticle;
import com.github.al.realworld.api.command.UnfavoriteArticleResult;
import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.command.UpdateArticleResult;
import com.github.al.realworld.api.query.GetArticle;
import com.github.al.realworld.api.query.GetArticleResult;
import com.github.al.realworld.api.query.GetArticles;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.api.query.GetComments;
import com.github.al.realworld.api.query.GetCommentsResult;
import com.github.al.realworld.api.query.GetFeed;
import com.github.al.realworld.api.query.GetFeedResult;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.bus.Bus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final Bus bus;
    private final AuthenticationService auth;

    @GetMapping("")
    public GetArticlesResult findByFilters(@RequestParam(name = "tag", required = false) String tag,
                                           @RequestParam(name = "author", required = false) String author,
                                           @RequestParam(name = "favorited", required = false) String favorited,
                                           @RequestParam(name = "limit", required = false) Integer limit,
                                           @RequestParam(name = "offset", required = false) Integer offset) {
        return bus.executeQuery(GetArticles.builder()
                .currentUsername(auth.currentUsername())
                .tag(tag)
                .author(author)
                .favorited(favorited)
                .limit(limit)
                .offset(offset)
                .build());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public CreateArticleResult create(@Valid @RequestBody CreateArticle command) {
        return bus.executeCommand(command.toBuilder().currentUsername(auth.currentUsername()).build());
    }

    @GetMapping("/feed")
    public GetFeedResult feed() {
        return bus.executeQuery(new GetFeed(auth.currentUsername()));
    }

    @GetMapping("/{slug}")
    public GetArticleResult findBySlug(@PathVariable String slug) {
        return bus.executeQuery(new GetArticle(auth.currentUsername(), slug));
    }

    @PutMapping("/{slug}")
    public UpdateArticleResult updateBySlug(@PathVariable String slug,
                                            @Valid @RequestBody UpdateArticle command) {
        return bus.executeCommand(command.toBuilder().slug(slug).currentUsername(auth.currentUsername()).build());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{slug}")
    public void deleteBySlug(@PathVariable String slug) {
        bus.executeCommand(new DeleteArticle(auth.currentUsername(), slug));
    }

    @PostMapping("/{slug}/favorite")
    public FavoriteArticleResult favorite(@PathVariable String slug) {
        return bus.executeCommand(new FavoriteArticle(auth.currentUsername(), slug));
    }

    @DeleteMapping("/{slug}/favorite")
    public UnfavoriteArticleResult unfavorite(@PathVariable String slug) {
        return bus.executeCommand(new UnfavoriteArticle(auth.currentUsername(), slug));
    }

    @GetMapping("/{slug}/comments")
    public GetCommentsResult findAllComments(@PathVariable String slug) {
        return bus.executeQuery(new GetComments(auth.currentUsername(), slug));
    }

    @PostMapping("/{slug}/comments")
    public AddCommentResult addComment(@PathVariable String slug,
                                       @Valid @RequestBody AddComment command) {
        return bus.executeCommand(command.toBuilder().slug(slug).currentUsername(auth.currentUsername()).build());
    }

    @DeleteMapping("/{slug}/comments/{id}")
    public void deleteComment(@PathVariable String slug,
                              @PathVariable Long id) {
        bus.executeCommand(new DeleteComment(auth.currentUsername(), slug, id));
    }

}
