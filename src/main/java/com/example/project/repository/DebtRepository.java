package com.example.project.repository;

import com.example.project.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtRepository extends JpaRepository<Debt, Integer> {
}