package ru.videoplatform.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoginDto {

    @JsonProperty("login")
    private String login;

    @JsonProperty("password")
    private String password;
}
