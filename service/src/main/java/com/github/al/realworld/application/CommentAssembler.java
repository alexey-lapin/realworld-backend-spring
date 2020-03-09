package com.github.al.realworld.application;

import com.github.al.realworld.api.dto.CommentDto;
import com.github.al.realworld.domain.Comment;

public class CommentAssembler {

    public static CommentDto assemble(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .body(comment.getBody())
                .author(ProfileAssembler.assemble(comment.getAuthor()))
                .build();
    }

}
