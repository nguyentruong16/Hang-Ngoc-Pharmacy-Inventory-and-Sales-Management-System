package com.example.project.repository;

import com.example.project.entity.Vatinvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VatinvoiceRepository extends JpaRepository<Vatinvoice, Integer> {
}