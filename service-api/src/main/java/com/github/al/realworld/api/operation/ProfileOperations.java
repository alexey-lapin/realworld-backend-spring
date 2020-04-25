package com.github.al.realworld.api.operation;

import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.api.command.UnfollowProfileResult;
import com.github.al.realworld.api.query.GetProfileResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

public interface ProfileOperations {

    @GetMapping("/profiles/{username}")
    GetProfileResult findByUsername(@PathVariable("username") String username);

    @PostMapping("/profiles/{username}/follow")
    FollowProfileResult follow(@PathVariable("username") String username);

    @DeleteMapping("/profiles/{username}/follow")
    UnfollowProfileResult unfollow(@PathVariable("username") String username);

}
