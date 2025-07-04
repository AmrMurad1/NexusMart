package ecommerce.shopapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;

}
