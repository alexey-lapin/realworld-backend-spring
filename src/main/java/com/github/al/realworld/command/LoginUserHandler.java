package com.github.al.realworld.command;

import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.UserRepository;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.web.dto.UserDetails;
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
        return new LoginUserResult(UserDetails.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build());
    }
}
