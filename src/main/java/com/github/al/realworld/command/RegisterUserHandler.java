package com.github.al.realworld.command;

import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.UserRepository;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.web.dto.UserDetails;
import com.github.al.realworld.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RegisterUserHandler implements CommandHandler<RegisterUserResult, RegisterUser> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Transactional
    @Override
    public RegisterUserResult handle(RegisterUser cmd) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(cmd.getEmail());
        if (userByEmailOptional.isPresent()) {
            throw new RuntimeException("user with email already exists");
        }
        Optional<User> userByUsernameOptional = userRepository.findByUsername(cmd.getUsername());
        if (userByUsernameOptional.isPresent()) {
            throw new RuntimeException("user with username already exists");
        }
        //todo encrypt password
        User user = new User(cmd.getEmail(), cmd.getUsername(), cmd.getPassword());
        User savedUser = userRepository.save(user);
        return new RegisterUserResult(UserDetails.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build());
    }
}
