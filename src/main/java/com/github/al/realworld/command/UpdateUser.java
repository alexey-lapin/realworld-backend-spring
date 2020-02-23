package com.github.al.realworld.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@JsonRootName("user")
public class UpdateUser implements Command<UpdateUserResult> {

    @Email
    private String email;
    private String username;
    private String password;
    private String image;
    private String bio;
}
