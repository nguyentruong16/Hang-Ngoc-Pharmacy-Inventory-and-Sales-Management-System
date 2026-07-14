package com.example.project.repository;

import com.example.project.entity.Financialsetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialsettingRepository extends JpaRepository<Financialsetting, Integer> {
    Optional<Financialsetting> findFirstByOrderByIdAsc();
}