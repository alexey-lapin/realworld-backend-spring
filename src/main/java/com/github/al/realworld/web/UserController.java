package com.github.al.realworld.web;

import com.github.al.realworld.application.query.GetCurrentUser;
import com.github.al.realworld.application.query.GetCurrentUserQ;
import com.github.al.realworld.application.query.GetCurrentUserResult;
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.command.*;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.service.JwtService;
import com.github.al.realworld.service.UserService;
import com.github.al.realworld.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    private final Bus bus;

//    @PostMapping("/users/login")
//    public UserDto authenticate(@Valid @RequestBody UserAuthenticationCommand cmd) {
//        return userService.login(cmd);
//    }

    @PostMapping("/users/login")
    public LoginUserResult login(@Valid @RequestBody LoginUser cmd) {
        return bus.executeCommand(cmd);
    }

    @PostMapping("/users")
    public RegisterUserResult register(@Valid @RequestBody RegisterUser cmd) {
        return bus.executeCommand(cmd);
    }

//    @GetMapping("/user")
//    public GetCurrentUserResult current(@AuthenticationPrincipal User user) {
//        return bus.executeQuery(new GetCurrentUserQ(user));
//    }

    @GetMapping("/user")
    public GetCurrentUser.Result current(@AuthenticationPrincipal User user) {
        return bus.executeQuery(new GetCurrentUser.Query(user));
    }


    @PutMapping("/user")
    public UpdateUserResult update(@AuthenticationPrincipal User user, @RequestBody UpdateUser cmd) {
        return bus.executeCommand(cmd);
    }

}
