/*
 * MIT License
 *
 * Copyright (c) 2019 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.domain;

import com.github.al.realworld.domain.model.Profile;
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
