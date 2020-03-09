package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.query.GetTags;
import com.github.al.realworld.api.query.GetTagsResult;
import com.github.al.realworld.bus.Bus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tags")
public class TagsController {

    private final Bus bus;

    @GetMapping
    public GetTagsResult findAll() {
        return bus.executeQuery(new GetTags());
    }

}
