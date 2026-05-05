package com.yoot.flashcard.modules.content.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "languages")
@SequencedDocument("languages")
public class Language {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String code;

    private String name;

    @Indexed
    private boolean active = true;
}
