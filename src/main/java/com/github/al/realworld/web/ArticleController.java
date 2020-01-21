package com.github.al.realworld.web;

import com.github.al.realworld.command.ArticleCreateCommand;
import com.github.al.realworld.service.ArticleService;
import com.github.al.realworld.web.dto.ArticleDto;
import com.github.al.realworld.web.dto.ArticlesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/")
    public ArticlesDto findAll() {
        return articleService.findAll();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto create(@Valid @RequestBody ArticleCreateCommand cmd) {
        return articleService.create(cmd);
    }

}
