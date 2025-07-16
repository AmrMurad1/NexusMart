package amrmurad.nexusmart.services;

import amrmurad.nexusmart.entities.User;
import amrmurad.nexusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (!StringUtils.hasText(email)){
            log.warn("Attempted to load user with empty or null email");
            throw new UsernameNotFoundException("email must be not empty");
        }

        String trimmedEmail = email.trim();
        log.debug("Attempting to load user by email: {}", trimmedEmail);

        // Retrieve user from database
        User user = userRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", trimmedEmail);
                    return new UsernameNotFoundException("User not found with email: " + trimmedEmail);
                });

        log.debug("User found: {}", user.getEmail());

        // Return Spring Security User object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
