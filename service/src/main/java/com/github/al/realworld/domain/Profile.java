package com.github.al.realworld.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Profile {

    @EqualsAndHashCode.Include
    @Id
    private String username;
    private String bio;
    private String image;

    @Singular
    @ManyToMany
    @JoinTable(name = "followers",
            joinColumns = @JoinColumn(name = "follower", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "followee", referencedColumnName = "username")
    )
    private Set<Profile> followers;

}
