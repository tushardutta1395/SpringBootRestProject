package org.studyeasy.SpringRestDemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.studyeasy.SpringRestDemo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(final String email);
}
