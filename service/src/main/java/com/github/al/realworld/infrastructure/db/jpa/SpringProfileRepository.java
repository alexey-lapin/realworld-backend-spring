package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringProfileRepository extends CrudRepository<Profile, String> {

    Optional<Profile> findByUsername(String username);

}
