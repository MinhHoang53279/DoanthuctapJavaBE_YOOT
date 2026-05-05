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
@Document(collection = "reports")
@SequencedDocument("reports")
public class Report {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private User reporter;

    @Indexed
    private ReportTargetType targetType;

    @Indexed
    private Long targetId;

    private String reason;

    @Indexed
    private ReportStatus status = ReportStatus.OPEN;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
