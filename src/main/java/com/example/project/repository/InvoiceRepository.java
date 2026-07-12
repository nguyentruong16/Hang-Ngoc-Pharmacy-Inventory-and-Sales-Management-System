package com.example.project.repository;

import com.example.project.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    /** All invoices with the relations the list screen renders, avoiding N+1 lazy loads. */
    @Query("""
       select i
       from Invoice i
       left join fetch i.employeeID
       left join fetch i.customerID
       """)
    List<Invoice> findAllWithRelations();
}
