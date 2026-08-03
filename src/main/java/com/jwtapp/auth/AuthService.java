package com.jwtapp.auth;

import com.jwtapp.auth.dto.*;
import com.jwtapp.security.JwtService;
import com.jwtapp.token.RefreshToken;
import com.jwtapp.token.RefreshTokenRepository;
import com.jwtapp.user.AppUser;
import com.jwtapp.user.Role;
import com.jwtapp.user.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        return createAndPersistTokens(user, null);
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

        return createAndPersistTokens(user, null);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenValue = request.refreshToken();

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        AppUser user = storedToken.getUser();

        try {
            if (!jwtService.isTokenType(tokenValue, "refresh") || !jwtService.isTokenValid(tokenValue, user)) {
                storedToken.setRevoked(true);
                refreshTokenRepository.save(storedToken);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }
        } catch (JwtException | IllegalArgumentException e) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return createAndPersistTokens(user, storedToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository
                .findByTokenAndRevokedFalse(request.refreshToken())
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Authentication authentication) {
        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        return new UserInfoResponse(user.getId(), user.getEmail(), user.getRole());
    }

    private AuthResponse createAndPersistTokens(AppUser user, RefreshToken tokenToReuse) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = tokenToReuse != null
                ? tokenToReuse
                : refreshTokenRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());

        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationInMs()));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpirationInMs());
    }
}
