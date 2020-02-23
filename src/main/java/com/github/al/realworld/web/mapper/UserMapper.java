package com.github.al.realworld.web.mapper;

import com.github.al.realworld.domain.User;
import com.github.al.realworld.web.dto.UserDetails;
import com.github.al.realworld.web.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user, String token) {
        return new UserDto(UserDetails.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(token)
                .build());
    }


}
