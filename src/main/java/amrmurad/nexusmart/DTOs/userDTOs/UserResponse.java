package amrmurad.nexusmart.DTOs.userDTOs;

import amrmurad.nexusmart.enums.Role;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
