package com.mutuals.common.validation;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void acceptsPasswordWithAllCharacterTypes() {
        assertThat(validator.isValid("Str0ng!Pass", null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1!", "alllowercase1!", "ALLUPPERCASE1!", "NoNumbers!!", "NoSymbols123"})
    void rejectsWeakPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void rejectsNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}