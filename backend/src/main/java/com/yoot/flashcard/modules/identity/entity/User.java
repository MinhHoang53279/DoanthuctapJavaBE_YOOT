package com.yoot.flashcard.modules.identity.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
@SequencedDocument("users")
public class User {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    @Indexed
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Indexed
    private LocalDateTime deletedAt;

    private UserProfile profile;

    @DBRef
    private Set<Role> roles = new HashSet<>();
}
