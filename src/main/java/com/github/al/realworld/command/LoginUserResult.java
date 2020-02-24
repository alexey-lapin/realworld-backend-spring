package com.github.al.realworld.command;

import com.github.al.realworld.web.dto.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginUserResult {

    private UserDetails user;

}
