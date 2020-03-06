package com.github.al.realworld.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Boolean favorited;
    private Integer favouritesCount;
    @OneToOne
    private Profile author;
    @OneToMany(cascade = CascadeType.ALL)
    private final Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "article_favorites",
            joinColumns = @JoinColumn(name = "article_slug"),
            inverseJoinColumns = @JoinColumn(name = "username")
    )
    private final Set<Profile> favoritedProfiles = new HashSet<>();

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

    public Article addFavorite(Profile profile) {
        favoritedProfiles.add(profile);
        return this;
    }

    public Article removeFavorite(Profile profile) {
        favoritedProfiles.remove(profile);
        return this;
    }
}
