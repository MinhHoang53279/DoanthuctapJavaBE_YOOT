package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends MongoRepository<Topic, Long> {

    List<Topic> findByActiveTrueOrderByNameAsc();

    Optional<Topic> findByName(String name);

    boolean existsByName(String name);
}
