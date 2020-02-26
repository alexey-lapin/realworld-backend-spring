package com.github.al.realworld.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagsDto {

    private Iterable<String> tags;

}
