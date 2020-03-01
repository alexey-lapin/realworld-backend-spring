package com.github.al.realworld.api;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Builder
@Getter
public class CommentDto {

    private Long id;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String body;
    // author;


}
