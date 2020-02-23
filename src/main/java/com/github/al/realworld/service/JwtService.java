package com.github.al.realworld.service;

import com.github.al.realworld.domain.User;

public interface JwtService {

    String getSubject(String token);

    String getToken(User user);

}
