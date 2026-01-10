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

class DefaultSlugServiceTest {

    private static final String RANDOM_SUFFIX_PATTERN = "[a-z0-9]{12}";

    private static SlugService slugService;

    @BeforeAll
    static void beforeAll() {
        slugService = new DefaultSlugService();
    }

    @Test
    void should_appendRandomSuffix_when_inputHasNoSeparators() {
        String slug = slugService.makeSlug("qwer");

        assertThat(slug).matches("qwer-" + RANDOM_SUFFIX_PATTERN);
    }

    @Test
    void should_trimTrailingHyphen_when_inputEndsWithHyphen() {
        String slug = slugService.makeSlug("qwer-");

        assertThat(slug).matches("qwer-" + RANDOM_SUFFIX_PATTERN);
    }

    @Test
    void should_replaceWhitespaceWithHyphen_when_inputContainsWhitespace() {
        String slug = slugService.makeSlug("qw er-");

        assertThat(slug).matches("qw-er-" + RANDOM_SUFFIX_PATTERN);
    }

    @Test
    void should_lowercaseAndReplaceWhitespace_when_inputHasUppercaseAndWhitespace() {
        String slug = slugService.makeSlug("Qw er-");

        assertThat(slug).matches("qw-er-" + RANDOM_SUFFIX_PATTERN);
    }

    @Test
    void should_returnRandomSuffix_when_inputHasNoLatinCharacters() {
        String slug = slugService.makeSlug("!!!");

        assertThat(slug).matches(RANDOM_SUFFIX_PATTERN);
    }


    @Test
    void should_trimTrailingHyphen_when_inputEndsWithHyphenCalled() {
        String slug = slugService.makeSlug("qwer-");

        assertSlugMatches("qwer", slug);
    }

    @Test
    void should_replaceWhitespaceWithHyphen_when_inputHasSpacesCalled() {
        String slug = slugService.makeSlug("qw er-");

        assertSlugMatches("qw-er", slug);
    }

    @Test
    void should_lowercaseOutput_when_inputHasUppercaseCalled() {
        String slug = slugService.makeSlug("Qw Er-");

        assertSlugMatches("qw-er", slug);
    }

    @Test
    void should_collapseRepeatedHyphens_when_inputHasMultipleHyphensCalled() {
        String slug = slugService.makeSlug("qw--er---");

        assertSlugMatches("qw-er", slug);
    }

    @Test
    void should_replacePunctuationWithHyphen_when_inputHasPunctuationCalled() {
        String slug = slugService.makeSlug("qw.er,");

        assertSlugMatches("qw-er", slug);
    }

    @Test
    void should_returnRandomSuffixOnly_when_inputHasNoSlugCharsCalled() {
        String slug = slugService.makeSlug("!!!");

        assertThat(slug).matches(RANDOM_SUFFIX_PATTERN);
    }

    private static void assertSlugMatches(String expectedBase, String slug) {
        assertThat(slug).matches(expectedBase + "-" + RANDOM_SUFFIX_PATTERN);
    }

}
