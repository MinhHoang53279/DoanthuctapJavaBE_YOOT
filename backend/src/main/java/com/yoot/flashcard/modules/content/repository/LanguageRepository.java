package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Language;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends MongoRepository<Language, Long> {

    List<Language> findByActiveTrueOrderByNameAsc();

    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);
}
