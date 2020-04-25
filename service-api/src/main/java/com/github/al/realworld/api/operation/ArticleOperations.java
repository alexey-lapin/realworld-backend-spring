package com.github.al.realworld.api.operation;

import com.github.al.realworld.api.command.AddComment;
import com.github.al.realworld.api.command.AddCommentResult;
import com.github.al.realworld.api.command.CreateArticle;
import com.github.al.realworld.api.command.CreateArticleResult;
import com.github.al.realworld.api.command.FavoriteArticleResult;
import com.github.al.realworld.api.command.UnfavoriteArticleResult;
import com.github.al.realworld.api.command.UpdateArticle;
import com.github.al.realworld.api.command.UpdateArticleResult;
import com.github.al.realworld.api.query.GetArticleResult;
import com.github.al.realworld.api.query.GetArticlesResult;
import com.github.al.realworld.api.query.GetCommentsResult;
import com.github.al.realworld.api.query.GetFeedResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

public interface ArticleOperations {

    @GetMapping("/articles{?tag,author,favorited,limit,offset}")
    GetArticlesResult findByFilters(@RequestParam String tag,
                                    @RequestParam String author,
                                    @RequestParam String favorited,
                                    @RequestParam(defaultValue = "20") Integer limit,
                                    @RequestParam(defaultValue = "0") Integer offset);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/articles")
    CreateArticleResult create(@Valid @RequestBody CreateArticle command);

    @GetMapping("/articles/feed")
    GetFeedResult feed(@RequestParam(defaultValue = "20") Integer limit,
                       @RequestParam(defaultValue = "0") Integer offset);

    @GetMapping("/articles/{slug}")
    GetArticleResult findBySlug(@PathVariable("slug") String slug);

    @PutMapping("/articles/{slug}")
    UpdateArticleResult updateBySlug(@PathVariable("slug") String slug, @Valid @RequestBody UpdateArticle command);

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/articles/{slug}")
    void deleteBySlug(@PathVariable("slug") String slug);

    @PostMapping("/articles/{slug}/favorite")
    FavoriteArticleResult favorite(@PathVariable("slug") String slug);

    @DeleteMapping("/articles/{slug}/favorite")
    UnfavoriteArticleResult unfavorite(@PathVariable("slug") String slug);

    @GetMapping("/articles/{slug}/comments")
    GetCommentsResult findAllComments(@PathVariable("slug") String slug);

    @PostMapping("/articles/{slug}/comments")
    AddCommentResult addComment(@PathVariable("slug") String slug, @Valid @RequestBody AddComment command);

    @DeleteMapping("/articles/{slug}/comments/{id}")
    void deleteComment(@PathVariable("slug") String slug, @PathVariable("id") Long id);

}
