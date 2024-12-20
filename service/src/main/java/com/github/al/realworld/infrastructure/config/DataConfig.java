package com.github.al.realworld.infrastructure.config;

import com.github.al.realworld.infrastructure.db.jdbc.AbstractSequentialIdEntity;
import com.github.al.realworld.infrastructure.db.jdbc.AbstractUuidIdEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@EnableJpaRepositories(basePackages = "com.github.al.realworld.infrastructure.db.jpa")
@EnableJdbcRepositories(basePackages = "com.github.al.realworld.infrastructure.db.jdbc")
@Configuration(proxyBeanMethods = false)
public class DataConfig {

    @Bean
    public BeforeConvertCallback<AbstractSequentialIdEntity> sequentialIdCallback(JdbcTemplate jdbcTemplate) {
        return (entity) -> {
            if (entity.getId() == 0) {
                Long nextId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
                entity.setId(nextId);
            }
            return entity;
        };
    }

    @Bean
    public BeforeConvertCallback<AbstractUuidIdEntity> uuidIdEntityCallback() {
        return (entity) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        };
    }

}
