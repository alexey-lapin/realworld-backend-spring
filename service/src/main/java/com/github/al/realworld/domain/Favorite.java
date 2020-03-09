package com.github.al.realworld.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Favorite {

    @Id
    @GeneratedValue
    private Long id;
    private String profile;
    private String slug;

}
