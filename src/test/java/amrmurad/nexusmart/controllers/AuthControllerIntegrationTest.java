package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.DTOs.AuthResponse;
import amrmurad.nexusmart.DTOs.LoginRequest;
import amrmurad.nexusmart.DTOs.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @Test
    void testUserRegistrationAndLogin() {
        // ✅ 1. Register new user
        UserRegistrationRequest registerRequest = new UserRegistrationRequest();
        registerRequest.setUsername("amr");
        registerRequest.setEmail("amr@example.com");
        registerRequest.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserRegistrationRequest> registerEntity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<AuthResponse> registerResponse = restTemplate
                .postForEntity(getBaseUrl() + "/register", registerEntity, AuthResponse.class);

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertNotNull(registerResponse.getBody().getToken());

        // ✅ 2. Login with same credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("amr@example.com");
        loginRequest.setPassword("123456");

        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<AuthResponse> loginResponse = restTemplate
                .postForEntity(getBaseUrl() + "/login", loginEntity, AuthResponse.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertEquals("amr@example.com", loginResponse.getBody().getEmail());
        assertNotNull(loginResponse.getBody().getToken());
    }
}
