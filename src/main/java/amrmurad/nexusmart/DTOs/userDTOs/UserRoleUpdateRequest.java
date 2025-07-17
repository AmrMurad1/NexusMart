package amrmurad.nexusmart.DTOs.userDTOs;

import amrmurad.nexusmart.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequest {
    @NotNull(message = "Role is required")
    private Role role;
}