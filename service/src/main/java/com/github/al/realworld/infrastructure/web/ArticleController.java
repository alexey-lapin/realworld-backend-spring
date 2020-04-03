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
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("")
    public GetArticlesResult findByFilters(@AuthenticationPrincipal User user,
                                           @RequestParam(name = "tag", required = false) String tag,
                                           @RequestParam(name = "author", required = false) String author,
                                           @RequestParam(name = "favorited", required = false) String favorited,
                                           @RequestParam(name = "limit", required = false) Integer limit,
                                           @RequestParam(name = "offset", required = false) Integer offset) {
        return bus.executeQuery(GetArticles.builder()
                .currentUsername(safeUsername(user))
                .tag(tag)
                .author(author)
                .favorited(favorited)
                .limit(limit)
                .offset(offset)
                .build());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public CreateArticleResult create(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody CreateArticle command) {
        return bus.executeCommand(command.toBuilder().currentUsername(safeUsername(user)).build());
    }

    @GetMapping("/feed")
    public GetFeedResult feed(@AuthenticationPrincipal User user) {
        return bus.executeQuery(new GetFeed(safeUsername(user)));
    }

    @GetMapping("/{slug}")
    public GetArticleResult findBySlug(@AuthenticationPrincipal User user,
                                       @PathVariable String slug) {
        return bus.executeQuery(new GetArticle(safeUsername(user), slug));
    }

    @PutMapping("/{slug}")
    public UpdateArticleResult updateBySlug(@AuthenticationPrincipal User user,
                                            @PathVariable String slug,
                                            @Valid @RequestBody UpdateArticle command) {
        return bus.executeCommand(command.toBuilder().slug(slug).currentUsername(safeUsername(user)).build());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{slug}")
    public void deleteBySlug(@AuthenticationPrincipal User user,
                             @PathVariable String slug) {
        bus.executeCommand(new DeleteArticle(safeUsername(user), slug));
    }

    @PostMapping("/{slug}/favorite")
    public FavoriteArticleResult favorite(@AuthenticationPrincipal User user,
                                          @PathVariable String slug) {
        return bus.executeCommand(new FavoriteArticle(safeUsername(user), slug));
    }

    @DeleteMapping("/{slug}/favorite")
    public UnfavoriteArticleResult unfavorite(@AuthenticationPrincipal User user,
                                              @PathVariable String slug) {
        return bus.executeCommand(new UnfavoriteArticle(safeUsername(user), slug));
    }

    @GetMapping("/{slug}/comments")
    public GetCommentsResult findAllComments(@AuthenticationPrincipal User user,
                                             @PathVariable String slug) {
        return bus.executeQuery(new GetComments(safeUsername(user), slug));
    }

    @PostMapping("/{slug}/comments")
    public AddCommentResult addComment(@AuthenticationPrincipal User user,
                                       @PathVariable String slug,
                                       @Valid @RequestBody AddComment command) {
        return bus.executeCommand(command.toBuilder().slug(slug).currentUsername(safeUsername(user)).build());
    }

    @DeleteMapping("/{slug}/comments/{id}")
    public void deleteComment(@AuthenticationPrincipal User user,
                              @PathVariable String slug,
                              @PathVariable Long id) {
        bus.executeCommand(new DeleteComment(safeUsername(user), slug, id));
    }

    private String safeUsername(User user) {
        if (user != null) {
            return user.getUsername();
        }
        return null;
    }

}
