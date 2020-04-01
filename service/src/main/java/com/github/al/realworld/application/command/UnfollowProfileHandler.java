package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.UnfollowProfile;
import com.github.al.realworld.api.command.UnfollowProfileResult;
import com.github.al.realworld.application.ProfileAssembler;
import com.github.al.realworld.application.exception.ResourceNotFoundException;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * follower - one who follows someone (current user)
 * followee - is one who is followed
 */
@RequiredArgsConstructor
@Service
public class UnfollowProfileHandler implements CommandHandler<UnfollowProfileResult, UnfollowProfile> {

    private final ProfileRepository profileRepository;

    @Transactional
    @Override
    public UnfollowProfileResult handle(UnfollowProfile command) {
        Profile currentProfile = profileRepository.findByUsername(command.getFollower())
                .orElseThrow(RuntimeException::new);

        Profile followee = profileRepository.findByUsername(command.getFollowee())
                .orElseThrow(ResourceNotFoundException::new);

        Set<Profile> alteredFollowers = followee.getFollowers().stream()
                .filter(profile -> !Objects.equals(profile, currentProfile))
                .collect(Collectors.toSet());

        Profile alteredProfile = followee.toBuilder()
                .clearFollowers()
                .followers(alteredFollowers).build();
        Profile savedProfile = profileRepository.save(alteredProfile);

        return new UnfollowProfileResult(ProfileAssembler.assemble(savedProfile, currentProfile));
    }
}
