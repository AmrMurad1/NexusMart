package amrmurad.nexusmart.services;
import amrmurad.nexusmart.DTOs.AuthResponse;
import amrmurad.nexusmart.DTOs.LoginRequest;
import amrmurad.nexusmart.DTOs.UserRegistrationRequest;
import amrmurad.nexusmart.DTOs.UserUpdateRequest;
import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.enums.Role;
import amrmurad.nexusmart.repository.UserRepository;
import amrmurad.nexusmart.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


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

    public User updateUser(Long id, UserUpdateRequest request) {
        User existingUser = getUserById(id);

        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()){
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
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