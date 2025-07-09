package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.DTOs.UserUpdateRequest;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.services.UserService;
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
    public ResponseEntity<User> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request, Principal principal){
        String email = principal.getName();
        User updateUser = userService.updateUser(email, request);
        return ResponseEntity.ok(updateUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
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
