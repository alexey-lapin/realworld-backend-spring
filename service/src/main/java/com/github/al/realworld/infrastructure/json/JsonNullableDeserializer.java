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
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserializes {@link JsonNullable}, mapping an absent property to {@code null} and a JSON null
 * to an instance holding {@code null}.
 *
 * <p>The contained type is resolved from the declaring property, so a single instance serves
 * any type argument.
 */
public class JsonNullableDeserializer extends ValueDeserializer<JsonNullable<?>> {

    private final @Nullable JavaType contentType;

    public JsonNullableDeserializer() {
        this(null);
    }

    private JsonNullableDeserializer(@Nullable JavaType contentType) {
        this.contentType = contentType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        return new JsonNullableDeserializer(property.getType().containedType(0));
    }

    @Override
    public JsonNullable<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        if (contentType == null) {
            return new JsonNullable<>(ctxt.readValue(p, Object.class));
        }
        return new JsonNullable<>(ctxt.readValue(p, contentType));
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) {
        return new JsonNullable<>(null);
    }

    @Override
    public @Nullable Object getAbsentValue(DeserializationContext ctxt) {
        return null;
    }

}
