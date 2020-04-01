package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.command.UpdateUserResult;
import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.application.exception.ResourceNotFoundException;
import com.github.al.realworld.application.service.JwtService;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class UpdateUserHandler implements CommandHandler<UpdateUserResult, UpdateUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    @Override
    public UpdateUserResult handle(UpdateUser command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(ResourceNotFoundException::new);

        return new UpdateUserResult(UserDto.builder()
                .token(jwtService.getToken(user))
                .build());
    }
}
