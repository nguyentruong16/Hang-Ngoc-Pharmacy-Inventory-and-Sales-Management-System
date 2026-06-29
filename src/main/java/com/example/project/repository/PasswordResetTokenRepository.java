package com.example.project.repository;

import com.example.project.entity.Account;
import com.example.project.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Rate limiting: true if this account already requested a reset since {@code threshold}. */
    boolean existsByAccountIDAndCreatedAtAfter(Account accountID, LocalDateTime threshold);
}
