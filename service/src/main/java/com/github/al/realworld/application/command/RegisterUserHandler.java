package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.command.RegisterUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RegisterUserHandler implements CommandHandler<RegisterUserResult, RegisterUser> {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtService jwtService;

    @Transactional
    @Override
    public RegisterUserResult handle(RegisterUser command) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(command.getEmail());
        if (userByEmailOptional.isPresent()) {
            throw new RuntimeException("user with email already exists");
        }
        Optional<User> userByUsernameOptional = userRepository.findByUsername(command.getUsername());
        if (userByUsernameOptional.isPresent()) {
            throw new RuntimeException("user with username already exists");
        }

        Profile profile = new Profile(command.getUsername());
        profileRepository.save(profile);

        //todo encrypt password
        User user = new User(command.getUsername(), command.getEmail(), command.getPassword(), profile);
        User savedUser = userRepository.save(user);
        return new RegisterUserResult(UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build());
    }
}
