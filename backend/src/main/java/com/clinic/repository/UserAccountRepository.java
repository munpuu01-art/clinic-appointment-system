package com.clinic.repository;

import com.clinic.domain.auth.Role;
import com.clinic.domain.auth.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<UserAccount> findByRole(Role role);

    Optional<UserAccount> findByPersonId(Long personId);

    boolean existsByPersonId(Long personId);
}
