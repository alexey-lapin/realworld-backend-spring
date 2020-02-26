package com.github.al.realworld.application.query;

import com.github.al.realworld.api.ProfileDto;
import com.github.al.realworld.api.query.GetProfile;
import com.github.al.realworld.api.query.GetProfileResult;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetProfileHandler implements QueryHandler<GetProfileResult, GetProfile> {

    private final ProfileRepository profileRepository;

    @Override
    public GetProfileResult handle(GetProfile query) {

        Profile profile = profileRepository.findByUsername(query.getUsername()).orElseThrow(() -> new RuntimeException(""));
        return new GetProfileResult(new ProfileDto(profile.getUsername(), profile.getBio(), profile.getImage(), null));
    }
}
