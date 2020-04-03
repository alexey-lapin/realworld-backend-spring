package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.LoginUser;
import com.github.al.realworld.api.command.LoginUserResult;
import com.github.al.realworld.api.command.RegisterUser;
import com.github.al.realworld.api.command.RegisterUserResult;
import com.github.al.realworld.api.command.UpdateUser;
import com.github.al.realworld.api.command.UpdateUserResult;
import com.github.al.realworld.api.query.GetCurrentUser;
import com.github.al.realworld.api.query.GetCurrentUserResult;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.bus.Bus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final Bus bus;
    private final AuthenticationService auth;

    @PostMapping("/users/login")
    public LoginUserResult login(@Valid @RequestBody LoginUser cmd) {
        return bus.executeCommand(cmd);
    }

    @PostMapping("/users")
    public RegisterUserResult register(@Valid @RequestBody RegisterUser cmd) {
        return bus.executeCommand(cmd);
    }

    @GetMapping("/user")
    public GetCurrentUserResult current() {
        return bus.executeQuery(new GetCurrentUser(auth.currentUsername()));
    }

    @PutMapping("/user")
    public UpdateUserResult update(@Valid @RequestBody UpdateUser cmd) {
        //auth.currentUsername()
        return bus.executeCommand(cmd);
    }

}
