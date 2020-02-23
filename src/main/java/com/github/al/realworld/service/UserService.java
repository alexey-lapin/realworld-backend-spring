package com.github.al.realworld.service;

import com.github.al.realworld.command.UserAuthenticationCommand;
import com.github.al.realworld.command.RegisterUser;
import com.github.al.realworld.web.dto.UserDto;

public interface UserService {

    UserDto register(RegisterUser cmd);

    UserDto login(UserAuthenticationCommand cmd);

}
