package com.yoot.flashcard.modules.identity.repository;

import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "profile"})
    @Query("select u from User u where u.id = :id and u.deletedAt is null")
    Optional<User> findWithRolesById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "profile"})
    @Query("select u from User u where u.id = :id and u.deletedAt is null")
    Optional<User> findWithRolesAndPermissionsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "profile"})
    @Query("""
            select u from User u
            where u.deletedAt is null
              and (
                lower(u.email) = lower(:usernameOrEmail)
                or lower(u.username) = lower(:usernameOrEmail)
              )
            """)
    Optional<User> findWithRolesAndPermissionsByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    @EntityGraph(attributePaths = {"roles"})
    @Query("""
            select u from User u
            where u.deletedAt is null
              and (:status is null or u.status = :status)
              and (
                :keyword is null
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(u.username) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
