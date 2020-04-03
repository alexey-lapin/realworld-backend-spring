package com.github.al.realworld.infrastructure.db.jpa;

import com.github.al.realworld.domain.model.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringTagRepository extends CrudRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

}
