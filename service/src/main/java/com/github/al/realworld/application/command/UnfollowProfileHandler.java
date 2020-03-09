package com.github.al.realworld.application.command;

import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.api.command.UnfollowProfile;
import com.github.al.realworld.api.command.UnfollowProfileResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.repository.FollowRepository;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UnfollowProfileHandler implements CommandHandler<UnfollowProfileResult, UnfollowProfile> {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    @Override
    public UnfollowProfileResult handle(UnfollowProfile command) {
        Profile followee = profileRepository.findByUsername(command.getFollowee()).orElseThrow(() -> new RuntimeException("notf"));
        followRepository.deleteByFollowerAndFollowee(command.getFollower(), command.getFollowee());
        return new UnfollowProfileResult(new ProfileDto(followee.getUsername(), followee.getBio(), followee.getImage(), false));
    }
}
