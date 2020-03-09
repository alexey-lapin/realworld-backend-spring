package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.*;
import com.github.al.realworld.api.query.*;
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final Bus bus;

    @GetMapping("")
    public GetArticlesResult findByFilters(@AuthenticationPrincipal User user,
                                           @RequestParam(name = "tag", required = false) String tag,
                                           @RequestParam(name = "author", required = false) String author,
                                           @RequestParam(name = "favorited", required = false) String favorited,
                                           @RequestParam(name = "limit", required = false) Integer limit,
                                           @RequestParam(name = "offset", required = false) Integer offset) {
        return bus.executeQuery(GetArticles.builder()
                .username(user != null ? user.getUsername() : null)
                .tag(tag)
                .author(author)
                .favorited(favorited)
                .limit(limit)
                .offset(offset)
                .build());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateArticleResult create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateArticle command) {
        return bus.executeCommand(command.toBuilder().username(user.getUsername()).build());
    }

    @GetMapping("/feed")
    public GetFeedResult feed(@AuthenticationPrincipal User user) {
        return bus.executeQuery(new GetFeed(user.getUsername()));
    }

    @GetMapping("/{slug}")
    public GetArticleResult findBySlug(@AuthenticationPrincipal User user, @PathVariable String slug) {
        return bus.executeQuery(new GetArticle(slug, safeUsername(user)));
    }

    private String safeUsername(User user) {
        if (user != null) {
            return user.getUsername();
        }
        return null;
    }

    @PutMapping("/{slug}")
    public UpdateArticleResult updateBySlug(@AuthenticationPrincipal User user,
                                            @PathVariable String slug,
                                            @Valid @RequestBody UpdateArticle command) {
        return bus.executeCommand(command.toBuilder().slug(slug).username(user.getUsername()).build());
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBySlug(@PathVariable String slug) {
        bus.executeCommand(new DeleteArticle(slug));
    }

    @PostMapping("/{slug}/favorite")
    public FavoriteArticleResult favorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        return bus.executeCommand(new FavoriteArticle(slug, user.getUsername()));
    }

    @DeleteMapping("/{slug}/favorite")
    public UnfavoriteArticleResult unfavorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        return bus.executeCommand(new UnfavoriteArticle(slug, user.getUsername()));
    }

    @GetMapping("/{slug}/comments")
    public GetCommentsResult findAllComments(@PathVariable String slug) {
        return bus.executeQuery(new GetComments(slug));
    }

    @PostMapping("/{slug}/comments")
    public AddCommentResult addComment(@AuthenticationPrincipal User user, @PathVariable String slug,
                                       @RequestBody AddComment command) {
        return bus.executeCommand(command.toBuilder().slug(slug).username(user.getUsername()).build());
    }

    @DeleteMapping("/{slug}/comments/{id}")
    public void deleteComment(@PathVariable String slug,
                              @PathVariable Long id) {
        bus.executeCommand(new DeleteComment(slug, id));
    }

}
