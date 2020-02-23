package com.github.al.realworld.service;

import com.github.al.realworld.command.UserAuthenticationCommand;
import com.github.al.realworld.command.RegisterUser;
import com.github.al.realworld.domain.User;
import com.github.al.realworld.domain.UserRepository;
import com.github.al.realworld.web.dto.UserDto;
import com.github.al.realworld.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DefaultJwtService jwtService;

    @Override
    @Transactional
    public UserDto register(RegisterUser cmd) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(cmd.getEmail());
        if(userByEmailOptional.isPresent()) {
            throw new RuntimeException("user with email already exists");
        }
        Optional<User> userByUsernameOptional = userRepository.findByUsername(cmd.getUsername());
        if(userByUsernameOptional.isPresent()) {
            throw new RuntimeException("user with username already exists");
        }
        //todo encrypt password
        User user = new User(cmd.getEmail(), cmd.getUsername(), cmd.getPassword());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser, jwtService.getToken(savedUser));
    }

    @Override
    @Transactional
    public UserDto login(UserAuthenticationCommand cmd) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(cmd.getEmail());
        if(!userByEmailOptional.isPresent()) {
            throw new RuntimeException("user not found");
        }
        User user = userByEmailOptional.get();
        return userMapper.toDto(user, jwtService.getToken(user));
    }
}
