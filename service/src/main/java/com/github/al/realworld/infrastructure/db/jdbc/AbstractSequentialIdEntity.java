package com.github.al.realworld.infrastructure.db.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public abstract class AbstractSequentialIdEntity {

    @Id
    private long id;

}
