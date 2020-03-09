package com.github.al.realworld.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NoArgsConstructor
@Getter
@Entity
public class Follow {

    @Id
    @GeneratedValue
    private Long id;
    private String follower;
    private String followee;

    public Follow(String follower, String followee) {
        this.follower = follower;
        this.followee = followee;
    }
}
