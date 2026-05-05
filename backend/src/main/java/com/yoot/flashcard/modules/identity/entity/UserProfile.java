package com.yoot.flashcard.modules.identity.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    private Long id;

    private String fullName;

    private String avatarUrl;

    private String nativeLanguageCode;

    private String targetLanguageCode;

    private String timezone;

    private String bio;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
