package com.jwtapp.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Auto-generates secure key

    public String generateToken(UserDetails userdetails) {
        return Jwts.builder()
                .setSubject(userdetails.getUsername())
                .signWith(key)
                .compact();
    }
}
