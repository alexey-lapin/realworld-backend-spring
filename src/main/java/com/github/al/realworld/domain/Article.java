package com.github.al.realworld.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
public class Article {

    @Id
    private String slug;
    private String title;
    private String description;
    @Lob
    private String body;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "article_tags",
            joinColumns = @JoinColumn(name = "article_slug"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private final Set<Tag> tags = new HashSet<>();
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Boolean favorited;
    private Integer favouritesCount;
    //author
    @OneToMany(cascade = CascadeType.ALL)
    private final Set<Comment> comments = new HashSet<>();

    public Article addTag(Tag tag) {
        tags.add(tag);
        return this;
    }

    public Article removeTag(Tag tag) {
        tags.remove(tag);
        return this;
    }

    public Article addComment(Comment comment) {
        comments.add(comment);
        return this;
    }

    public Article removeComment(Comment comment) {
        comments.remove(comment);
        return this;
    }
}
