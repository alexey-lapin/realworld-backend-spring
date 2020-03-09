package com.github.al.realworld.application.command;

import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.api.command.FollowProfile;
import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Follow;
import com.github.al.realworld.domain.repository.FollowRepository;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FollowProfileHandler implements CommandHandler<FollowProfileResult, FollowProfile> {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    @Override
    public FollowProfileResult handle(FollowProfile command) {
        Profile followee = profileRepository.findByUsername(command.getFollowee()).orElseThrow(() -> new RuntimeException("prof not f"));
        followRepository.save(new Follow(command.getFollower(), command.getFollowee()));
        return new FollowProfileResult(new ProfileDto(followee.getUsername(), followee.getBio(), followee.getImage(), true));
    }
}
