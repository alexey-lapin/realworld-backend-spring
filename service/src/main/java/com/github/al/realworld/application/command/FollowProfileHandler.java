package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.FollowProfile;
import com.github.al.realworld.api.command.FollowProfileResult;
import com.github.al.realworld.application.ProfileAssembler;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * follower - one who follows someone (current user)
 * followee - is one who is followed
 */
@RequiredArgsConstructor
@Service
public class FollowProfileHandler implements CommandHandler<FollowProfileResult, FollowProfile> {

    private final ProfileRepository profileRepository;

    @Transactional
    @Override
    public FollowProfileResult handle(FollowProfile command) {
        Profile currentProfile = profileRepository.findByUsername(command.getFollower())
                .orElseThrow(RuntimeException::new);

        Profile followee = profileRepository.findByUsername(command.getFollowee())
                .orElseThrow(() -> new RuntimeException("prof not f"));

        Profile alteredProfile = followee.toBuilder().follower(currentProfile).build();
        Profile savedProfile = profileRepository.save(alteredProfile);

        return new FollowProfileResult(ProfileAssembler.assemble(savedProfile, currentProfile));
    }
}
