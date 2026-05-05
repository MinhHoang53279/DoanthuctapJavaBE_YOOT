package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TagRepository extends MongoRepository<Tag, Long> {

    List<Tag> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
