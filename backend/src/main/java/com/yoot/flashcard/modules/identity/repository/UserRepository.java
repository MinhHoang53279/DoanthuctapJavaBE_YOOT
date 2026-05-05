package com.yoot.flashcard.modules.identity.repository;

import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatus(UserStatus status);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<User> findWithRolesById(Long id);

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<User> findWithRolesAndPermissionsById(Long id);

    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<User> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);

    default Optional<User> findWithRolesAndPermissionsByUsernameOrEmail(String usernameOrEmail) {
        return findByEmailIgnoreCaseAndDeletedAtIsNull(usernameOrEmail)
                .or(() -> findByUsernameIgnoreCaseAndDeletedAtIsNull(usernameOrEmail));
    }
}
