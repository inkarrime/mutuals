package com.mutuals.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern STRONG = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && STRONG.matcher(value).matches();
    }
}
