package com.example.project.repository;

import com.example.project.entity.Purchaseinvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseinvoiceRepository extends JpaRepository<Purchaseinvoice, Integer> {
}