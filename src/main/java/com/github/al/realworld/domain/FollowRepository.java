package com.github.al.realworld.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FollowRepository extends CrudRepository<Follow, Long> {

    List<Follow> findByFollower(String profile);

    void deleteByFollowerAndFollowee(String follower, String followee);
}
