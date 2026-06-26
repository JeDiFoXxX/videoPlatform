package ru.videoplatform.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        boolean result = true;

        if (password == null || password.isEmpty()) {
            return result;
        }

        if (password.length() < 10 || password.length() > 20) {
            result = false;
        }

        if (result && !password.matches("^[a-zA-Z0-9!@#$%^&*]+$")) {
            result = false;
        }

        if (result && !password.matches(".*[a-z].*")) {
            result = false;
        }

        if (result && !password.matches(".*[A-Z].*")) {
            result = false;
        }

        if (result && !password.matches(".*[0-9].*")) {
            result = false;
        }

        if (result) {
            String specialChars = password.replaceAll("[a-zA-Z0-9]", "");
            if (specialChars.length() < 3) {
                result = false;
            }
        }

        return result;
    }
}
