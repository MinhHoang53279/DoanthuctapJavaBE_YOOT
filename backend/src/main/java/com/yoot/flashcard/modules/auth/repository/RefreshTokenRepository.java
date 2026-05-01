package com.yoot.flashcard.modules.auth.repository;

import com.yoot.flashcard.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = {"user", "user.roles", "user.roles.permissions", "user.profile"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
