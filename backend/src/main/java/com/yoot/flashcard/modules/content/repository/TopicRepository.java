package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByActiveTrueOrderByNameAsc();

    boolean existsByName(String name);
}
