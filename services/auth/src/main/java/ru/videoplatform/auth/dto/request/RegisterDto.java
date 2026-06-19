package ru.videoplatform.auth.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.videoplatform.auth.validation.ValidPassword;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RegisterDto {
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @JsonProperty("login")
    private String login;
    
    @ValidPassword
    @JsonProperty("password")
    private String password;
}
