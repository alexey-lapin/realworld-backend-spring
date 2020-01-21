package com.github.al.realworld.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
public class User {

//    @Id
//    @GeneratedValue
//    private Long id;
    @Id
    private String email;
    private String username;
    private String password;

}
