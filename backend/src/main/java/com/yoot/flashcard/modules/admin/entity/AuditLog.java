package com.yoot.flashcard.modules.admin.entity;

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
@Document(collection = "audit_logs")
@SequencedDocument("audit_logs")
public class AuditLog {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private User actor;

    @Indexed
    private String action;

    @Indexed
    private String resourceType;

    @Indexed
    private Long resourceId;

    private String details;

    @Indexed
    private LocalDateTime createdAt;
}
