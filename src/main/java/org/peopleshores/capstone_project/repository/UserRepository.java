package org.peopleshores.capstone_project.repository;

import org.peopleshores.capstone_project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /** Login path: only active accounts may authenticate. */
    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmail(String email);

    Page<User> findByActive(boolean active, Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.name = :roleName AND u.active = true")
    java.util.List<User> findActiveByRoleName(@Param("roleName") String roleName);
}
