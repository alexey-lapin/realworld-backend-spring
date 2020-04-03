package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.Tag;
import com.github.al.realworld.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JpaTagRepositoryAdapter implements TagRepository {

    private final SpringTagRepository repository;

    @Override
    public Optional<Tag> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Iterable<Tag> findAll() {
        return repository.findAll();
    }
}
