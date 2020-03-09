package com.github.al.realworld.api.command;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.github.al.realworld.bus.Command;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@JsonRootName("user")
public class RegisterUser implements Command<RegisterUserResult> {

    @Email
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}
