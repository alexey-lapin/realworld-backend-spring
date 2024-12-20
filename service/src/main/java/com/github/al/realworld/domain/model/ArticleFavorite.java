package com.github.al.realworld.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ArticleFavorite {

    private final UUID articleId;
    private final UUID userId;

}
