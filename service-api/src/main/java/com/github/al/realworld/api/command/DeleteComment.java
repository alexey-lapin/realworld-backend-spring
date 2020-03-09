package com.github.al.realworld.api.command;

import com.github.al.realworld.bus.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeleteComment implements Command<DeleteCommentResult> {

    private String slug;
    private Long id;

}
