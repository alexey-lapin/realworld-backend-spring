package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringUserRepository extends CrudRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

}
