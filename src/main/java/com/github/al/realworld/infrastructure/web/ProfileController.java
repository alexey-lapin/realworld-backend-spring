package com.github.al.realworld.infrastructure.web;

import com.github.al.realworld.api.command.FollowProfile;
import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.api.command.UnfollowProfile;
import com.github.al.realworld.api.command.UnfollowProfileResult;
import com.github.al.realworld.api.query.GetProfile;
import com.github.al.realworld.api.query.GetProfileResult;
import com.github.al.realworld.bus.Bus;
import com.github.al.realworld.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final Bus bus;

    @GetMapping("/{username}")
    public GetProfileResult findByUsername(@PathVariable String username) {
        return bus.executeQuery(new GetProfile(username));
    }

    @PostMapping("/{username}/follow")
    public FollowProfileResult follow(@AuthenticationPrincipal User user, @PathVariable String username) {
        return bus.executeCommand(new FollowProfile(user.getUsername(), username));
    }

    @DeleteMapping("/{username}/follow")
    public UnfollowProfileResult unfollow(@AuthenticationPrincipal User user, @PathVariable String username) {
        return bus.executeCommand(new UnfollowProfile(user.getUsername(), username));
    }

}
