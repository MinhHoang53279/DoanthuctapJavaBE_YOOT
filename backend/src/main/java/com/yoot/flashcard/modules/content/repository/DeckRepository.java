package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.DeckStatus;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    @EntityGraph(attributePaths = {
            "sourceLanguage",
            "targetLanguage",
            "topic",
            "createdBy",
            "approvedBy",
            "tags"
    })
    @Query("select d from Deck d where d.id = :id and d.deletedAt is null")
    Optional<Deck> findActiveById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"sourceLanguage", "targetLanguage", "topic", "createdBy"})
    @Query("""
            select d from Deck d
            where d.deletedAt is null
              and (
                :canViewAll = true
                or (d.visibility = :publicVisibility and d.status = :approvedStatus)
                or (:currentUserId is not null and d.createdBy.id = :currentUserId)
              )
              and (:keyword is null
                or lower(d.title) like lower(concat('%', :keyword, '%'))
                or lower(d.description) like lower(concat('%', :keyword, '%')))
              and (:topicId is null or d.topic.id = :topicId)
              and (:languageId is null or d.sourceLanguage.id = :languageId or d.targetLanguage.id = :languageId)
              and (:visibility is null or d.visibility = :visibility)
            """)
    Page<Deck> searchDecks(
            @Param("currentUserId") Long currentUserId,
            @Param("canViewAll") boolean canViewAll,
            @Param("publicVisibility") DeckVisibility publicVisibility,
            @Param("approvedStatus") DeckStatus approvedStatus,
            @Param("keyword") String keyword,
            @Param("topicId") Long topicId,
            @Param("languageId") Long languageId,
            @Param("visibility") DeckVisibility visibility,
            Pageable pageable
    );
}
