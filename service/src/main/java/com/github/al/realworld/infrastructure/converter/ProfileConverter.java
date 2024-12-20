package com.github.al.realworld.infrastructure.converter;

import com.github.al.realworld.api.dto.ProfileDto;
import com.github.al.realworld.domain.model.ProfileAssembly;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProfileConverter implements Converter<ProfileAssembly, ProfileDto> {

    @Override
    public ProfileDto convert(ProfileAssembly source) {
        return new ProfileDto(source.user().getUsername(),
                source.user().getBio(),
                source.user().getImage(),
                source.following());
    }

}
