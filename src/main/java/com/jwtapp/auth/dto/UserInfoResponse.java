package com.jwtapp.auth.dto;

import com.jwtapp.user.Role;

public record UserInfoResponse(Long id, String email, Role role) {
}