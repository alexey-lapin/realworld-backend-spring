package com.github.al.realworld.api.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonRootName("comment")
public class AddComment implements Command<AddCommentResult> {

    private String slug;
    private String body;
    private String username;

}
