package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
