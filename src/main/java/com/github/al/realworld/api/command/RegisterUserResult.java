package com.github.al.realworld.api.command;

import com.github.al.realworld.infrastructure.web.dto.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegisterUserResult {

    private UserDetails user;

}
