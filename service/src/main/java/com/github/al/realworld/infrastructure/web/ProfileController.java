package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.FollowProfile;
import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.api.command.UnfollowProfile;
import com.github.al.realworld.api.command.UnfollowProfileResult;
import com.github.al.realworld.api.query.GetProfile;
import com.github.al.realworld.api.query.GetProfileResult;
import com.github.al.realworld.application.service.AuthenticationService;
import com.github.al.realworld.bus.Bus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final Bus bus;
    private final AuthenticationService auth;

    @GetMapping("/{username}")
    public GetProfileResult findByUsername(@PathVariable String username) {
        return bus.executeQuery(new GetProfile(auth.currentUsername(), username));
    }

    @PostMapping("/{username}/follow")
    public FollowProfileResult follow(@PathVariable String username) {
        return bus.executeCommand(new FollowProfile(auth.currentUsername(), username));
    }

    @DeleteMapping("/{username}/follow")
    public UnfollowProfileResult unfollow(@PathVariable String username) {
        return bus.executeCommand(new UnfollowProfile(auth.currentUsername(), username));
    }

}
