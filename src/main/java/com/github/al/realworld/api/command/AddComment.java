package com.github.al.realworld.api.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import com.github.al.realworld.domain.Profile;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@JsonRootName("comment")
public class AddComment implements Command<AddCommentResult> {

    private String slug;
    private String body;
    private Profile profile;

}
