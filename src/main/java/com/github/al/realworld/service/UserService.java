package com.github.al.realworld.service;

import com.github.al.realworld.application.command.UserAuthenticationCommand;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.infrastructure.web.dto.UserDto;

public interface UserService {

    UserDto register(RegisterUser cmd);

    UserDto login(UserAuthenticationCommand cmd);

}
