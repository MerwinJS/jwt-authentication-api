package com.jwtapp.token;

import com.jwtapp.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    Optional<RefreshToken> findFirstByUserOrderByCreatedAtDesc(AppUser user);

    List<RefreshToken> findAllByUserAndRevokedFalse(AppUser user);
}
