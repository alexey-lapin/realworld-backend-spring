package com.github.al.realworld.web;

import com.github.al.realworld.command.UserRegistrationCommand;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.service.UserService;
import com.github.al.realworld.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final UserService userService;

    @GetMapping("/users/login")
    public UserDto authenticate() {
        return null;
    }

    @PostMapping("/users")
    public UserDto register(@RequestBody @Valid UserRegistrationCommand cmd) {
        return userService.register(cmd);
    }

    @GetMapping("/user")
    public UserDto current(@AuthenticationPrincipal User currentUser) {
        System.out.println("Current USer = " + currentUser);
        return null;
    }

    @PutMapping("/user")
    public UserDto update() {
        return null;
    }

}
