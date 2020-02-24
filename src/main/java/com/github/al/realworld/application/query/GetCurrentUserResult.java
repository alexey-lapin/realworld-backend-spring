package com.github.al.realworld.application.query;

import com.github.al.realworld.web.dto.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetCurrentUserResult {

    private UserDetails user;

}
