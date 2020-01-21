package com.github.al.realworld.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Getter
@Entity
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    @Lob
    private String body;

    @ManyToOne
    private Article article;
    //author

}
