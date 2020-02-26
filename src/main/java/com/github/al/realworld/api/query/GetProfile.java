package com.github.al.realworld.api.query;

import com.github.al.realworld.bus.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetProfile implements Query<GetProfileResult> {

    private String username;

}
