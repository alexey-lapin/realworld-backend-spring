/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
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
package com.github.al.realworld.application.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugServiceTest {

    private static SlugService slugService;

    @BeforeAll
    static void beforeAll() {
        slugService = new SlugService();
    }

    @Test
    void name1() {
        String slug = slugService.makeSlug("qwer");

        assertThat(slug).isEqualTo("qwer");
    }

    @Test
    void name2() {
        String slug = slugService.makeSlug("qwer-");

        assertThat(slug).isEqualTo("qwer");
    }

    @Test
    void name3() {
        String slug = slugService.makeSlug("qw er-");

        assertThat(slug).isEqualTo("qw-er");
    }

    @Test
    void name4() {
        String slug = slugService.makeSlug("Qw er-");

        assertThat(slug).isEqualTo("qw-er");
    }
}
