package com.github.al.realworld.api.query;

import com.github.al.realworld.api.query.GetCurrentUserResult;
import com.github.al.realworld.bus.Query;
import com.github.al.realworld.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetCurrentUserQ implements Query<GetCurrentUserResult> {

    private User user;

}
