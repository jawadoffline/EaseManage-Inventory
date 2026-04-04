package com.easemanage.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String TEST_SECRET =
            "ThisIsATestSecretKeyThatIsAtLeast64CharactersLongForHMACSHA256AlgorithmUsage!!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 60000L);
        userDetails = new User(
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void generateAccessToken_createsValidToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
        // JWT tokens have 3 dot-separated parts
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateAccessToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtService.generateAccessToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtService.generateAccessToken(userDetails);

        UserDetails otherUser = new User(
                "otheruser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        // Create a JwtService with 0ms expiration so the token is immediately expired
        JwtService expiredJwtService = new JwtService(TEST_SECRET, 0L);
        String token = expiredJwtService.generateAccessToken(userDetails);

        boolean valid = expiredJwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }
}
