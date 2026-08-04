package com.mejs.jwtapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties(
                    "SGVsbGDSHuvu78djjDFLNADjbnl87SUuZTI1NkJpdHNMb25nIQ==",
                    60000,
                    120000
            )
    );

    @Test
    void shouldGenerateValidAccessToken() {
        UserDetails user = User.withUsername("unit@test.com").password("test").authorities("ROLE_USER").build();

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserName(token)).isEqualTo("unit@test.com");
        assertThat(jwtService.isTokenType(token, "access")).isTrue();
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        UserDetails user = User.withUsername("unit@test.com").password("test").authorities("ROLE_USER").build();

        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractUserName(token)).isEqualTo("unit@test.com");
        assertThat(jwtService.isTokenType(token, "refresh")).isTrue();
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

}