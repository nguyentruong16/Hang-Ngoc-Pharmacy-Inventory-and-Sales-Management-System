package com.example.project.repository;

import com.example.project.entity.Purchaseinvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurchaseinvoiceRepository extends JpaRepository<Purchaseinvoice, Integer> {

    @Query("""
           select p
           from Purchaseinvoice p
           left join fetch p.supplierID
           left join fetch p.employeeID
           left join fetch p.procurementID
           order by p.date desc
           """)
    List<Purchaseinvoice> findAllWithRelations();

    @Query("""
           select p
           from Purchaseinvoice p
           left join fetch p.supplierID
           left join fetch p.employeeID
           left join fetch p.procurementID
           where p.id = :id
           """)
    Optional<Purchaseinvoice> findByIdWithRelations(Integer id);
}