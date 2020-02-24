package com.github.al.realworld.bus;

public interface QueryHandler<R, Q extends Query<R>> {

    R handle(Q query);

}
