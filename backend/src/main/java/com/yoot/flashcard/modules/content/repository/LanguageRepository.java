package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    List<Language> findByActiveTrueOrderByNameAsc();

    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);
}
