package com.jwtapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtProperties.accessTokenExpiration(), "access");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtProperties.refreshTokenExpiration(), "refresh");
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenType(String token, String type) {
        String tokenType = extractAllClaims(token).get("type", String.class);
        return type.equals(tokenType);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public long getAccessTokenExpirationInMs() {
        return jwtProperties.accessTokenExpiration();
    }

    public long getRefreshTokenExpirationInMs() {
        return jwtProperties.refreshTokenExpiration();
    }

    private String generateToken(UserDetails userDetails, long expirationInMs, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        return Jwts
                .builder().claims(claims)
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationInMs)))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
