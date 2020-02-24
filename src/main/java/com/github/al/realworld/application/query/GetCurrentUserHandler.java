package com.github.al.realworld.application.query;

import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.web.dto.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetCurrentUserHandler implements QueryHandler<GetCurrentUserResult, GetCurrentUserQ> {

    private final JwtService jwtService;

    @Override
    public GetCurrentUserResult handle(GetCurrentUserQ query) {
        User user = query.getUser();
        return new GetCurrentUserResult(UserDetails.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.getToken(user))
                .build()
        );
    }
}
