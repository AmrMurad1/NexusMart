package amrmurad.nexusmart.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Email is invalid")
    private String email;

    private String password;
}
