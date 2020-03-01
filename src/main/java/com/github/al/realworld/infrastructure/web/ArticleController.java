package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.*;
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.infrastructure.web.dto.ArticleDto;
import com.github.al.realworld.infrastructure.web.dto.ArticlesDto;
import com.github.al.realworld.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    private final Bus bus;

    @GetMapping("")
    public ArticlesDto findAll() {
        return articleService.findAll();
    }

//    @PostMapping("")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ArticleDto create(@Valid @RequestBody ArticleCreateCommand cmd) {
//        return articleService.create(cmd);
//    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateArticleResult create(@Valid @RequestBody CreateArticle command) {
        return bus.executeCommand(command);
    }

    @GetMapping("/feed")
    public ArticlesDto feed(@AuthenticationPrincipal User user) {
        return null;
    }

    @GetMapping("/{slug}")
    public ArticleDto findBySlug(@PathVariable String slug) {
        return null;
    }

    @PutMapping("/{slug}")
    public ArticleDto updateBySlug(@PathVariable String slug) {
        return null;
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBySlug(@PathVariable String slug) {
        bus.executeCommand(new DeleteArticle(slug));
    }

    @PostMapping("/{slug}/favorite")
    public FavoriteArticleResult favorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        return bus.executeCommand(new FavoriteArticle(slug, user.getProfile()));
    }

    @DeleteMapping("/{slug}/favorite")
    public UnfavoriteArticleResult unfavorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        return bus.executeCommand(new UnfavoriteArticle(slug, user.getProfile()));
    }

    @PostMapping("/{slug}/comments")
    public AddCommentResult addComment(@AuthenticationPrincipal User user, @PathVariable String slug,
                                       @RequestBody AddComment command) {
        return bus.executeCommand(command.setSlug(slug).setProfile(user.getProfile()));
    }

    @DeleteMapping("/{slug}/comments/{id}")
    public void deleteComment(@AuthenticationPrincipal User user, @PathVariable String slug,
                              @PathVariable Long id) {
//        bus.executeCommand(command.setSlug(slug).setProfile(user.getProfile()));
    }

}
