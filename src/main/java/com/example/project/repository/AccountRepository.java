package com.example.project.repository;

import com.example.project.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    Optional<Account> findByEmailIgnoreCase(String email);
}
