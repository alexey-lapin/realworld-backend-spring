package com.github.al.realworld.domain.model;

public record ArticleAssembly(
        Article article,
        boolean favorited,
        int favoritesCount,
        ProfileAssembly author
) {
}
