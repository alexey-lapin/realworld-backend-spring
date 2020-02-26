package com.github.al.realworld.api.query;

import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.infrastructure.web.dto.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

public class GetCurrentUser {

    @AllArgsConstructor
    @Getter
    public static class Query implements com.github.al.realworld.bus.Query<Result> {
        private User user;
    }

    @AllArgsConstructor
    @Getter
    public static class Result {
        private UserDetails user;
    }

    @RequiredArgsConstructor
    @Component
    public static class Handler implements QueryHandler<Result, Query> {

        private final JwtService jwtService;

        @Override
        public Result handle(Query query) {
            User user = query.getUser();
            return new Result(UserDetails.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .token(jwtService.getToken(user))
                    .build()
            );
        }
    }


}
