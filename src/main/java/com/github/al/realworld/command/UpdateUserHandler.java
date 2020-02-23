package com.github.al.realworld.command;

import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.UserRepository;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.web.dto.UserDetails;
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
        return new UpdateUserResult(UserDetails.builder()
                .token(jwtService.getToken(byEmail))
                .build());
    }
}
