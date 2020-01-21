package com.github.al.realworld.service;

import com.github.al.realworld.command.UserRegistrationCommand;
import com.github.al.realworld.web.dto.UserDto;

public interface UserService {

    UserDto register(UserRegistrationCommand cmd);

}
