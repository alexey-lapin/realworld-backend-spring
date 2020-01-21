package com.github.al.realworld.service;

import com.github.al.realworld.command.UserRegistrationCommand;
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

    @Override
    @Transactional
    public UserDto register(UserRegistrationCommand cmd) {
        Optional<User> userByEmailOptional = userRepository.findByEmail(cmd.getEmail());
        if(userByEmailOptional.isPresent()) {
            throw new RuntimeException("user with email already exists");
        }
        Optional<User> userByUsernameOptional = userRepository.findByUsername(cmd.getUsername());
        if(userByUsernameOptional.isPresent()) {
            throw new RuntimeException("user with username already exists");
        }
        User user = new User(cmd.getEmail(), cmd.getUsername(), cmd.getPassword());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
