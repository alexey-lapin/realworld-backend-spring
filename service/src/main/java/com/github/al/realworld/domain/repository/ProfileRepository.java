package com.github.al.realworld.domain.repository;

import com.github.al.realworld.domain.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<Profile, String> {

    Optional<Profile> findByUsername(String username);

}
