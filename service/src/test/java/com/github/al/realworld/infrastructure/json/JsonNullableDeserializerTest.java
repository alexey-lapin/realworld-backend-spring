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
package com.github.al.realworld.infrastructure.json;

import com.github.al.realworld.api.dto.JsonNullable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonNullableDeserializerTest {

    record Payload(
            @Nullable JsonNullable<String> bio,
            @Nullable JsonNullable<List<String>> tagList
    ) {

    }

    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JsonNullableModule())
            .build();

    private Payload read(String json) {
        return mapper.readValue(json, Payload.class);
    }

    @Test
    void should_beNullReference_whenPropertyAbsent() {
        assertThat(read("{}").bio()).isNull();
    }

    @Test
    void should_holdNullValue_whenPropertyExplicitlyNull() {
        var bio = read("{\"bio\":null}").bio();

        assertThat(bio).isNotNull();
        assertThat(bio.isNull()).isTrue();
        assertThat(bio.value()).isNull();
    }

    @Test
    void should_holdValue_whenPropertyPresent() {
        assertThat(read("{\"bio\":\"hello\"}").bio()).isEqualTo(new JsonNullable<>("hello"));
    }

    @Test
    void should_holdEmptyString_whenPropertyBlank() {
        assertThat(read("{\"bio\":\"\"}").bio()).isEqualTo(new JsonNullable<>(""));
    }

    @Test
    void should_resolveGenericContentType_whenPropertyIsList() {
        assertThat(read("{\"tagList\":[\"a\",\"b\"]}").tagList())
                .isEqualTo(new JsonNullable<>(List.of("a", "b")));
    }

    @Test
    void should_distinguishEmptyListFromNullAndAbsent() {
        assertThat(read("{\"tagList\":[]}").tagList()).isEqualTo(new JsonNullable<>(List.<String>of()));
        assertThat(read("{\"tagList\":null}").tagList()).isEqualTo(new JsonNullable<List<String>>(null));
        assertThat(read("{}").tagList()).isNull();
    }

    @Test
    void should_serializeAsBareValue() {
        var json = mapper.writeValueAsString(new Payload(new JsonNullable<>("hello"), new JsonNullable<>(null)));

        assertThat(json).isEqualTo("{\"bio\":\"hello\",\"tagList\":null}");
    }

}
