package com.yoot.flashcard.modules.auth.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.exception.ConflictException;
import com.yoot.flashcard.common.security.JwtProperties;
import com.yoot.flashcard.common.security.JwtService;
import com.yoot.flashcard.common.security.UserPrincipal;
import com.yoot.flashcard.modules.auth.dto.AuthTokenResponse;
import com.yoot.flashcard.modules.auth.dto.AuthUserResponse;
import com.yoot.flashcard.modules.auth.dto.LoginRequest;
import com.yoot.flashcard.modules.auth.dto.RegisterRequest;
import com.yoot.flashcard.modules.auth.entity.RefreshToken;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserProfile;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import com.yoot.flashcard.modules.identity.repository.RoleRepository;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "LEARNER";
    private static final String TOKEN_TYPE = "Bearer";
    private static final String INVALID_LOGIN_MESSAGE = "Invalid username/email or password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
    }
    public AuthUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }

        Role learnerRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role is not configured"));

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(learnerRole);

        UserProfile profile = new UserProfile();
        profile.setFullName(request.fullName());
        user.setProfile(profile);

        return toAuthUser(userRepository.save(user));
    }
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findWithRolesAndPermissionsByUsernameOrEmail(request.usernameOrEmail().trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN_MESSAGE));

        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN_MESSAGE);
        }

        return createTokenResponse(user);
    }
    public AuthTokenResponse refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);
        refreshTokenService.revoke(refreshToken);
        return createTokenResponse(refreshToken.getUser());
    }
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeForUser(rawRefreshToken, currentUserId());
    }
    public AuthUserResponse currentUser() {
        User user = userRepository.findWithRolesAndPermissionsById(currentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return toAuthUser(user);
    }

    private AuthTokenResponse createTokenResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                TOKEN_TYPE,
                jwtProperties.getAccessTokenExpirationMs() / 1000
        );
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal.getId();
    }

    private AuthUserResponse toAuthUser(User user) {
        String fullName = user.getProfile() == null ? null : user.getProfile().getFullName();
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                fullName,
                user.getStatus(),
                roles
        );
    }
}
