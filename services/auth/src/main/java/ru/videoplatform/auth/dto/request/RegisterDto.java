package ru.videoplatform.auth.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RegisterDto {
    @JsonProperty("login")
    private String login;

    @JsonProperty("password")
    private String password;
}
