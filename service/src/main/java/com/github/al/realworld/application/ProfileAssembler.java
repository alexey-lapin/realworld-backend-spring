package com.github.al.realworld.application;

import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.domain.Profile;

public class ProfileAssembler {

    public static ProfileDto assemble(Profile profile) {
        return new ProfileDto(profile.getUsername(), profile.getBio(), profile.getImage(), false);
    }

}
