package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.DTOs.AuthResponse;
import amrmurad.nexusmart.DTOs.LoginRequest;
import amrmurad.nexusmart.DTOs.UserRegistrationRequest;
import amrmurad.nexusmart.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestControllerAdvice
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request){
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser (@Valid @RequestBody LoginRequest request){
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }



}
