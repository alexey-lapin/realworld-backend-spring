package com.github.al.realworld.domain.repository;

import com.github.al.realworld.domain.Follow;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FollowRepository extends CrudRepository<Follow, Long> {

    List<Follow> findByFollower(String profile);

    void deleteByFollowerAndFollowee(String follower, String followee);
}
