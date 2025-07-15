package amrmurad.nexusmart.services;
import amrmurad.nexusmart.DTOs.AuthResponse;
import amrmurad.nexusmart.DTOs.LoginRequest;
import amrmurad.nexusmart.DTOs.UserRegistrationRequest;
import amrmurad.nexusmart.DTOs.UserUpdateRequest;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.enums.Role;
import amrmurad.nexusmart.exceptions.EmailAlreadyExistsException;
import amrmurad.nexusmart.repository.UserRepository;
import amrmurad.nexusmart.security.JwtService;
import org.springframework.util.StringUtils;
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

import java.security.PublicKey;
import java.util.List;
import java.util.Locale;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional (readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }


    public AuthResponse registerUser(UserRegistrationRequest request) {

         if (userRepository.findByEmail(request.getEmail().toLowerCase().trim()).isPresent()) {
            throw new IllegalArgumentException("this email is already registered: " + request.getEmail());
        }
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        log.info("user saved successfully: {}", savedUser.getEmail());

        String jwtToken = jwtService.generateToken(savedUser);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

    }

    public AuthResponse authenticateUser(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken
                            (request.getEmail().toLowerCase().trim(),
                             request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new UsernameNotFoundException("this user not found"));

            String jwtToken = jwtService.generateToken(user);
            log.info("User authenticated successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();


        } catch (BadCredentialsException bx) {
            log.warn("Authentication failed: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

    }

    public User updateUser(Long id, UserUpdateRequest request) {
        User existingUser = getUserById(id);

        if (StringUtils.hasText(request.getUsername())) {
            existingUser.setUsername(request.getUsername().trim());
        }

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
        User UpdatedUser = userRepository.save(existingUser);
        log.info("user updated successfully: {}", UpdatedUser.getEmail());
        return userRepository.save(existingUser);

    }
    public User updateUserByEmail(String email, UserUpdateRequest request) {
        User user = getUserByEmail(email);
        return updateUser(user.getId(), request);
    }

    public void deleteById (Long id){
        if (!userRepository.existsById(id)){
            throw new UsernameNotFoundException("user not found with Id: "+ id);
        }
        userRepository.deleteById(id);
        log.info("user deleted successfully with id: {}", id );
    }

    public void deleteByEmail(String email){
        User user = getUserByEmail(email);
        userRepository.delete(user);
    }

 }