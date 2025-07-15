package amrmurad.nexusmart.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email is invalid")
    private String email;

    // Additional profile fields can be added here
    private String firstName;
    private String lastName;
    private String phoneNumber;
}