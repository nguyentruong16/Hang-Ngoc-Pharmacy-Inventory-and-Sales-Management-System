package com.example.project.repository;

import com.example.project.entity.Invoicedetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoicedetailRepository extends JpaRepository<Invoicedetail, Integer> {
}