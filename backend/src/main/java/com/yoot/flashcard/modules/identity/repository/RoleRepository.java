package com.yoot.flashcard.modules.identity.repository;

import com.yoot.flashcard.modules.identity.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
