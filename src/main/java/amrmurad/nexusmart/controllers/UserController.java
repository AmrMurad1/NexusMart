package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.DTOs.*;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.enums.Role;
import amrmurad.nexusmart.repository.UserRepository;
import amrmurad.nexusmart.services.UserService;
import com.stripe.model.tax.Registration;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal){
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request, Principal principal){
        String email = principal.getName();
        User updateUser = userService.updateUserByEmail(email, request);
        UserResponse response = mapToUserResponse(updateUser);
        return ResponseEntity.ok(response);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                                @Valid @RequestBody PasswordChangeRequest request){
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, UserRoleUpdateRequest request){
        User updateUser = userService.updateUserRole(id, request);
        UserResponse response = mapToUserResponse(updateUser);
        return ResponseEntity.ok().build();
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser(Principal principal){
        String email = principal.getName();
        userService.deleteByEmail(email);
        return ResponseEntity.ok("your account has been deleted successfully");
    }


}




