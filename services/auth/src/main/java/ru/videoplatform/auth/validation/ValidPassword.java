package ru.videoplatform.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Пароль не соответствует требованиям безопасности";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
