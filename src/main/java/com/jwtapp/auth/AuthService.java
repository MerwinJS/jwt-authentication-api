package com.jwtapp.auth;

import com.jwtapp.auth.dto.AuthResponse;
import com.jwtapp.auth.dto.LoginRequest;
import com.jwtapp.auth.dto.RegisterRequest;
import com.jwtapp.auth.dto.UserInfoResponse;
import com.jwtapp.security.JwtService;
import com.jwtapp.token.RefreshToken;
import com.jwtapp.token.RefreshTokenRepository;
import com.jwtapp.user.AppUser;
import com.jwtapp.user.Role;
import com.jwtapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser user = AppUser.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return createAndPersistTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        return createAndPersistTokens(user);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Authentication authentication) {
        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return new UserInfoResponse(user.getId(), user.getEmail(), user.getRole());
    }

    private AuthResponse createAndPersistTokens(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken
                .builder()
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationInMs()))
                .user(user)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpirationInMs());
    }
}
