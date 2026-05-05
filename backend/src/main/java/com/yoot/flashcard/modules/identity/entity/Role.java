package com.yoot.flashcard.modules.identity.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "roles")
@SequencedDocument("roles")
public class Role {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String name;

    private String description;

    @DBRef
    private Set<Permission> permissions = new HashSet<>();
}
