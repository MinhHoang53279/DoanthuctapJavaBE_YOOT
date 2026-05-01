package com.yoot.flashcard.modules.identity.repository;

import com.yoot.flashcard.modules.identity.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(String name);
}
