package com.github.al.realworld.web;

import com.github.al.realworld.command.ArticleCreateCommand;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.service.ArticleService;
import com.github.al.realworld.web.dto.ArticleDto;
import com.github.al.realworld.web.dto.ArticlesDto;
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

    @GetMapping("")
    public ArticlesDto findAll() {
        return articleService.findAll();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto create(@Valid @RequestBody ArticleCreateCommand cmd) {
        return articleService.create(cmd);
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
    public void deleteBySlug(@PathVariable String slug) {

    }

}
