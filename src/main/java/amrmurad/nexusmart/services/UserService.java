package amrmurad.nexusmart.services;

import amrmurad.nexusmart.DTOs.userDTOs.*;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.enums.Role;
import amrmurad.nexusmart.exceptions.UserNotFoundException;
import amrmurad.nexusmart.exceptions.EmailAlreadyExistsException;
import amrmurad.nexusmart.repository.UserRepository;
import amrmurad.nexusmart.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );


    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        validateId(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        validateEmail(email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        validateEmail(email);
        return userRepository.findByEmail(email).isPresent();
    }

    public AuthResponse registerUser(UserRegistrationRequest request) {
        // Validation is now handled by Bean Validation annotations
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // Check for existing email in a transaction-safe way
        if (userRepository.findByEmail(request.getEmail().toLowerCase().trim()).isPresent()) {
            throw new EmailAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered with email: {}", savedUser.getEmail());

        String jwtToken = jwtService.generateToken(savedUser);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        // Validation is now handled by Bean Validation annotations
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String jwtToken = jwtService.generateToken(user);
            log.info("User authenticated successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public User updateUser(Long userId, UserUpdateRequest request) {
        validateUpdateRequest(request);

        User existingUser = getUserById(userId);

        // Update username if provided
        if (StringUtils.hasText(request.getUsername())) {
            existingUser.setUsername(request.getUsername().trim());
        }

        // Update email if provided and different from current
        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().equalsIgnoreCase(existingUser.getEmail())) {

            String newEmail = request.getEmail().toLowerCase().trim();

            // Check if new email already exists
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new EmailAlreadyExistsException("Email is already in use: " + newEmail);
            }

            existingUser.setEmail(newEmail);
        }

        // Update password if provided
        if (StringUtils.hasText(request.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return updatedUser;
    }

    public User updateUserByEmail(String email, UserUpdateRequest request) {
        User user = getUserByEmail(email);
        return updateUser(user.getId(), request);
    }

    public void deleteById(Long id) {
        validateId(id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    public void deleteByEmail(String email) {
        User user = getUserByEmail(email);
        userRepository.delete(user);
        log.info("User deleted successfully with email: {}", email);
    }

    public User changePassword(Long userId, PasswordChangeRequest request) {
        validateId(userId);

        if (request == null) {
            throw new IllegalArgumentException("Password change request cannot be null");
        }

        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());

        return updatedUser;
    }

    public User updateUserRole(Long userId, UserRoleUpdateRequest request) {
        validateId(userId);

        if (request == null || request.getRole() == null) {
            throw new IllegalArgumentException("Role update request and role cannot be null");
        }

        User user = getUserById(userId);
        user.setRole(request.getRole());
        User updatedUser = userRepository.save(user);
        log.info("Role updated to {} for user: {}", request.getRole(), user.getEmail());

        return updatedUser;
    }

    private void validateUpdateRequest(UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }

        // At least one field should be provided for update
        if (!StringUtils.hasText(request.getUsername()) &&
                !StringUtils.hasText(request.getEmail()) &&
                !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
    }

    // Removed redundant validation methods since Bean Validation handles them now
    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}