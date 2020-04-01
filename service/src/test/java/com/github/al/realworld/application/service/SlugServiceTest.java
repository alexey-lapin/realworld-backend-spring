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