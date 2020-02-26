package com.github.al.realworld.api.query;

import com.github.al.realworld.infrastructure.web.dto.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetCurrentUserResult {

    private UserDetails user;

}
