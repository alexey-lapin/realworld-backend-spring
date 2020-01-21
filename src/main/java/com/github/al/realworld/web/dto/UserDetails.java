package com.github.al.realworld.web.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDetails {

    private String email;
    private String token;
    private String username;
    private String bio;
    private String image;

}
