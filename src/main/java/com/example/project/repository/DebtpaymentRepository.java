package com.example.project.repository;

import com.example.project.entity.Debtpayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtpaymentRepository extends JpaRepository<Debtpayment, Integer> {
}