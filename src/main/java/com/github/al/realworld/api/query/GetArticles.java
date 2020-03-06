package com.github.al.realworld.api.query;

import com.github.al.realworld.bus.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetArticles implements Query<GetArticlesResult> {

    private String tag;
    private String author;
    private String favorited;
    private Integer limit;
    private Integer offset;

}
