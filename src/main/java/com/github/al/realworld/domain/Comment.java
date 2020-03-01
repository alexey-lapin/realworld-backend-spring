package com.github.al.realworld.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Entity
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    @Lob
    private String body;

//    @ManyToOne
//    private Article article;
    //author

}
