package com.github.al.realworld.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@JsonRootName("user")
public class UserAuthenticationCommand {

    @Email
    private String email;
    @NotBlank
    private String password;
}
