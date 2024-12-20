package com.github.al.realworld.infrastructure.db.jdbc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@RequiredArgsConstructor
@Getter
@Table("TBL_ARTICLE_TAGS")
public class ArticleTagRef {

    private final long tagId;

}
