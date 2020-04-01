package com.github.al.realworld.application.query;

import com.github.al.realworld.api.query.GetProfile;
import com.github.al.realworld.api.query.GetProfileResult;
import com.github.al.realworld.application.ProfileAssembler;
import com.github.al.realworld.application.exception.ResourceNotFoundException;
import com.github.al.realworld.bus.QueryHandler;
import com.github.al.realworld.domain.Profile;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.repository.ProfileRepository;
import com.github.al.realworld.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetProfileHandler implements QueryHandler<GetProfileResult, GetProfile> {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public GetProfileResult handle(GetProfile query) {
        Profile currentProfile = userRepository.findByUsername(query.getUsername())
                .map(User::getProfile)
                .orElse(null);

        Profile profile = userRepository.findByUsername(query.getUsername())
                .map(User::getProfile)
                .orElseThrow(ResourceNotFoundException::new);

        return new GetProfileResult(ProfileAssembler.assemble(profile, currentProfile));
    }
}