package com.yoot.flashcard.modules.identity.repository;

import com.yoot.flashcard.modules.identity.entity.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);
}
