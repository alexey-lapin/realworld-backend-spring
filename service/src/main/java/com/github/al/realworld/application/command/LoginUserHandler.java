package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.LoginUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LoginUserHandler implements CommandHandler<LoginUserResult, LoginUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    @Override
    public LoginUserResult handle(LoginUser command) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(command.getEmail());
        if (!userByEmailOptional.isPresent()) {
            throw new RuntimeException("user not found");
        }
        User user = userByEmailOptional.get();
        return new LoginUserResult(UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build());
    }
}
