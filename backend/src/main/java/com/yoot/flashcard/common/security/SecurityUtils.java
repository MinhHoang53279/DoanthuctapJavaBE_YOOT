package com.yoot.flashcard.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static Optional<Long> currentUserId() {
        return currentUser().map(UserPrincipal::getId);
    }

    public static boolean hasAnyRole(String... roles) {
        return currentUser()
                .map(principal -> Arrays.stream(roles).anyMatch(role -> principal.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role))))
                .orElse(false);
    }
}
