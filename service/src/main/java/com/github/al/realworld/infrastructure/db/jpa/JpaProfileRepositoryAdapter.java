package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.Profile;
import com.github.al.realworld.domain.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JpaProfileRepositoryAdapter implements ProfileRepository {

    private final SpringProfileRepository repository;

    @Override
    public Optional<Profile> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Profile save(Profile profile) {
        return repository.save(profile);
    }
}
