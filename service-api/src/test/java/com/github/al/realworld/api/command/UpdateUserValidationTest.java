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
package com.github.al.realworld.api.command;

import com.github.al.realworld.api.dto.JsonNullable;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static UpdateUser withEmail(JsonNullable<String> email) {
        return new UpdateUser(new UpdateUser.Data(email, null, null, null, null));
    }

    @Test
    void should_rejectMalformedEmail_underTheFieldsOwnKey() {
        var violations = validator.validate(withEmail(new JsonNullable<>("not-an-email")));

        assertThat(violations).hasSize(1);
        var violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("is invalid");

        var path = violation.getPropertyPath().toString();
        assertThat(path.substring(path.lastIndexOf('.') + 1)).isEqualTo("email");
    }

    @Test
    void should_acceptWellFormedEmail() {
        assertThat(validator.validate(withEmail(new JsonNullable<>("someone@example.com")))).isEmpty();
    }

    @Test
    void should_acceptAbsentEmail() {
        assertThat(validator.validate(withEmail(null))).isEmpty();
    }

    @Test
    void should_rejectExplicitNullEmail() {
        assertThat(validator.validate(withEmail(new JsonNullable<>(null))))
                .singleElement()
                .satisfies(violation -> assertThat(violation.getMessage()).isEqualTo("can't be blank"));
    }

}
