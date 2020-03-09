package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.*;
import com.github.al.realworld.api.query.GetCurrentUser;
import com.github.al.realworld.api.query.GetCurrentUserResult;
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final Bus bus;

    @PostMapping("/users/login")
    public LoginUserResult login(@Valid @RequestBody LoginUser cmd) {
        return bus.executeCommand(cmd);
    }

    @PostMapping("/users")
    public RegisterUserResult register(@Valid @RequestBody RegisterUser cmd) {
        return bus.executeCommand(cmd);
    }

    @GetMapping("/user")
    public GetCurrentUserResult current(@AuthenticationPrincipal User user) {
        return bus.executeQuery(new GetCurrentUser(user.getUsername()));
    }

    @PutMapping("/user")
    public UpdateUserResult update(@AuthenticationPrincipal User user, @RequestBody UpdateUser cmd) {
        return bus.executeCommand(cmd);
    }

}
