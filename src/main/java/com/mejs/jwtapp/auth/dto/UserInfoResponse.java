package com.mejs.jwtapp.auth.dto;

import com.mejs.jwtapp.user.Role;

public record UserInfoResponse(Long id, String email, Role role) {
}