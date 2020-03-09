package com.github.al.realworld.domain.repository;

import com.github.al.realworld.domain.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TagRepository extends CrudRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
}
