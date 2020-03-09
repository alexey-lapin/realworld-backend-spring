package com.github.al.realworld.application.query;

import com.github.al.realworld.api.dto.UserDto;
import com.github.al.realworld.api.query.GetCurrentUser;
import com.github.al.realworld.api.query.GetCurrentUserResult;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.UserRepository;
import com.github.al.realworld.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetCurrentUserHandler implements QueryHandler<GetCurrentUserResult, GetCurrentUser> {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public GetCurrentUserResult handle(GetCurrentUser query) {
        User user = userRepository.findByUsername(query.getUsername()).orElseThrow(() -> new RuntimeException(""));
        return new GetCurrentUserResult(UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build()
        );
    }
}
