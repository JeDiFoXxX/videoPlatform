package ru.videoplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.videoplatform.auth.validation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TeacherRegisterDto {

    @NotBlank(message = "Логин не может быть пустым")
    @Size(max = 20, message = "Логин не должен превышать 20 символов")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Логин может содержать только латинские буквы и цифры")
    @JsonProperty("login")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @ValidPassword
    @JsonProperty("password")
    private String password;
}
