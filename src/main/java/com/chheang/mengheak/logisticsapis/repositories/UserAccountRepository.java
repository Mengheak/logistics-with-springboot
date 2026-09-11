package com.chheang.mengheak.logisticsapis.repositories;

import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
