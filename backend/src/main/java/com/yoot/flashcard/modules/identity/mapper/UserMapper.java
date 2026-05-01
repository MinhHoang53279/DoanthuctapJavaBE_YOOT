package com.yoot.flashcard.modules.identity.mapper;

import com.yoot.flashcard.modules.identity.dto.UserDetailResponse;
import com.yoot.flashcard.modules.identity.dto.UserProfileResponse;
import com.yoot.flashcard.modules.identity.dto.UserSummaryResponse;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                roleNames(user)
        );
    }

    public UserDetailResponse toDetail(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                roleNames(user),
                toProfile(user.getProfile()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserProfileResponse toProfile(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return new UserProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getAvatarUrl(),
                profile.getNativeLanguageCode(),
                profile.getTargetLanguageCode(),
                profile.getTimezone(),
                profile.getBio()
        );
    }

    private Set<String> roleNames(User user) {
        if (user.getRoles() == null) {
            return Set.of();
        }

        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
