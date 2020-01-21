package com.github.al.realworld.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugifyTest {

    private static Slugify slugify;

    @BeforeAll
    static void beforeAll() {
        slugify = new Slugify();
    }

    @Test
    void name1() {
        String slug = slugify.makeSlug("qwer");

        assertThat(slug).isEqualTo("qwer");
    }

    @Test
    void name2() {
        String slug = slugify.makeSlug("qwer-");

        assertThat(slug).isEqualTo("qwer");
    }

    @Test
    void name3() {
        String slug = slugify.makeSlug("qw er-");

        assertThat(slug).isEqualTo("qw-er");
    }

    @Test
    void name4() {
        String slug = slugify.makeSlug("Qw er-");

        assertThat(slug).isEqualTo("qw-er");
    }
}