package com.github.al.realworld.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProfileTest {

    @Test
    void equalityTest() {
        Profile user1 = Profile.builder().username("user1").build();
        Profile user11 = Profile.builder().username("user1").build();
        Profile user111 = Profile.builder().username("user1").build();
        Profile user2 = Profile.builder().username("user2").build();

        assertAll(() -> {
            //reflexive
            assertThat(user1).isEqualTo(user1);
            //symmetric
            assertThat(user1).isEqualTo(user11);
            assertThat(user11).isEqualTo(user1);
            //transitive
            assertThat(user1).isEqualTo(user11);
            assertThat(user11).isEqualTo(user111);
            assertThat(user1).isEqualTo(user111);
            //null
            assertThat(user1).isNotEqualTo(null);

            assertThat(user1).isNotEqualTo(user2);
        });
    }
}