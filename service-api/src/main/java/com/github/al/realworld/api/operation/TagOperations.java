package com.github.al.realworld.api.operation;

import com.github.al.realworld.api.query.GetTagsResult;
import org.springframework.web.bind.annotation.GetMapping;

public interface TagOperations {

    @GetMapping("/tags")
    GetTagsResult findAll();

}
