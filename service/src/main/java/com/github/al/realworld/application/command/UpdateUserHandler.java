package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.command.UpdateUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Component
public class UpdateUserHandler implements CommandHandler<UpdateUserResult, UpdateUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    @Override
    public UpdateUserResult handle(UpdateUser command) {
        User byEmail = userRepository.findByEmail(command.getEmail()).orElseThrow(() -> new RuntimeException());
        return new UpdateUserResult(UserDto.builder()
                .token(jwtService.getToken(byEmail))
                .build());
    }
}
