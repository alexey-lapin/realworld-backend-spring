package com.github.al.realworld.infrastructure.db.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter
@Setter
public abstract class AbstractUuidIdEntity {

    @Id
    private UUID id;

}
