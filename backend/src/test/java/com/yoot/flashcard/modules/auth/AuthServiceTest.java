package com.yoot.flashcard.modules.auth;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.security.JwtProperties;
import com.yoot.flashcard.common.security.JwtService;
import com.yoot.flashcard.modules.auth.dto.AuthUserResponse;
import com.yoot.flashcard.modules.auth.dto.LoginRequest;
import com.yoot.flashcard.modules.auth.dto.RegisterRequest;
import com.yoot.flashcard.modules.auth.service.AuthService;
import com.yoot.flashcard.modules.auth.service.RefreshTokenService;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import com.yoot.flashcard.modules.identity.repository.RoleRepository;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesEmailHashesPasswordAndAssignsLearnerRole() {
        Role learnerRole = new Role();
        learnerRole.setName("LEARNER");

        when(userRepository.existsByEmail("learner@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("learner_user")).thenReturn(false);
        when(roleRepository.findByName("LEARNER")).thenReturn(Optional.of(learnerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        AuthUserResponse response = authService.register(new RegisterRequest(
                "Learner@Example.COM",
                "learner_user",
                "Password@123",
                "Learner User"
        ));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("learner@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("LEARNER");
        assertThat(savedUser.getProfile().getFullName()).isEqualTo("Learner User");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.roles()).containsExactly("LEARNER");
    }

    @Test
    void loginRejectsLockedUserBeforeIssuingTokens() {
        User lockedUser = new User();
        lockedUser.setId(20L);
        lockedUser.setEmail("locked@example.com");
        lockedUser.setUsername("locked_user");
        lockedUser.setPasswordHash("encoded-password");
        lockedUser.setStatus(UserStatus.LOCKED);

        when(userRepository.findWithRolesAndPermissionsByUsernameOrEmail("locked@example.com"))
                .thenReturn(Optional.of(lockedUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("locked@example.com", "Password@123")))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(passwordEncoder, never()).matches("Password@123", "encoded-password");
        verifyNoInteractions(jwtService, jwtProperties, refreshTokenService);
    }
}
