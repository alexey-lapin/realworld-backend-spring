package com.github.al.realworld.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

@NoArgsConstructor
@Getter
@Entity
public class Profile {

    @Id
    private String username;
    private String bio;
    private String image;
//    @MapsId
//    @OneToOne
//    private User user;


    public Profile(String username) {
        this.username = username;
    }
}
