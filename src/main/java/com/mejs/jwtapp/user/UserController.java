package com.mejs.jwtapp.user;

import com.mejs.jwtapp.auth.AuthService;
import com.mejs.jwtapp.auth.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/info")
    public UserInfoResponse getUserInfo(Authentication authentication) {
        return authService.getUserInfo(authentication);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminCheck(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "ADMIN check passed",
                "email", authentication.getName()
        ));
    }
}
