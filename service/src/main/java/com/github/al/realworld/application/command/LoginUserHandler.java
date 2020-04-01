package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.LoginUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.application.exception.ResourceNotFoundException;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LoginUserHandler implements CommandHandler<LoginUserResult, LoginUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    @Override
    public LoginUserResult handle(LoginUser command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(ResourceNotFoundException::new);

        return new LoginUserResult(UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build());
    }
}
