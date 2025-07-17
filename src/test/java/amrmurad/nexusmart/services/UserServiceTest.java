package amrmurad.nexusmart.services;

import amrmurad.nexusmart.DTOs.userDTOs.AuthResponse;
import amrmurad.nexusmart.DTOs.userDTOs.LoginRequest;
import amrmurad.nexusmart.DTOs.userDTOs.UserRegistrationRequest;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.enums.Role;
import amrmurad.nexusmart.repository.UserRepository;
import amrmurad.nexusmart.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("amr");
        registrationRequest.setEmail("amr@test.com");
        registrationRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("amr@test.com");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("amr");
        mockUser.setEmail("amr@test.com");
        mockUser.setPassword("hashed-password");
        mockUser.setRole(Role.USER);
    }

    @Test
    void givenValidRequest_whenRegisterUser_thenReturnAuthResponse() {
        // Given
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("hashed-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = userService.registerUser(registrationRequest);

        // Then
        assertEquals("amr@test.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenExistingEmail_whenRegisterUser_thenThrowException() {
        // Given
        when(userRepository.findByEmail(registrationRequest.getEmail()))
                .thenReturn(Optional.of(mockUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(registrationRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenValidCredentials_whenAuthenticateUser_thenReturnAuthResponse() {
        // Given
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");

        // When
        AuthResponse response = userService.authenticateUser(loginRequest);

        // Then
        assertEquals("amr@test.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void givenInvalidCredentials_whenAuthenticateUser_thenThrowBadCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> userService.authenticateUser(loginRequest));
    }

    @Test
    void givenValidId_whenGetUserById_thenReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("amr@test.com", result.getEmail());
    }

    @Test
    void givenInvalidId_whenGetUserById_thenThrowException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void givenInvalidId_whenDeleteById_thenThrowException() {
        // Given
        when(userRepository.existsById(99L)).thenReturn(false);

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.deleteById(99L));
    }

    @Test
    void givenValidId_whenDeleteById_thenSucceed() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteById(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }
}
