package com.yoot.flashcard.modules.auth.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.identity.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "refresh_tokens")
@SequencedDocument("refresh_tokens")
public class RefreshToken {

    @Id
    private Long id;

    @DBRef
    private User user;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed
    private LocalDateTime expiresAt;

    @Indexed
    private boolean revoked;

    private LocalDateTime revokedAt;

    private LocalDateTime createdAt;

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public void revoke(LocalDateTime now) {
        revoked = true;
        revokedAt = now;
    }
}
