package ecommerce.shopapp.services;
import ecommerce.shopapp.DTOs.AuthResponse;
import ecommerce.shopapp.DTOs.LoginRequest;
import ecommerce.shopapp.DTOs.UserRegistrationRequest;
import ecommerce.shopapp.entities.User;
import ecommerce.shopapp.enums.Role;
import ecommerce.shopapp.repository.UserRepository;
import ecommerce.shopapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }


    public AuthResponse registerUser(UserRegistrationRequest request) {

         if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("this email is already registered");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
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
                            (request.getEmail(), request.getPassword())
            );
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("this user not found"));

            String jwtToken = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();


        } catch (BadCredentialsException bx) {
            throw new BadCredentialsException("Invalid email or password");
        }

    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()){
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(existingUser);

    }

    public void deleteById (Long id){
        if (!userRepository.existsById(id)){
            throw new UsernameNotFoundException("user not found with Id: "+ id);
        }
        userRepository.deleteById(id);
    }

 }