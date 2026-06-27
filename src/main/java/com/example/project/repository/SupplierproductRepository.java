package com.example.project.repository;

import com.example.project.entity.Supplierproduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierproductRepository extends JpaRepository<Supplierproduct, Integer> {

    long countBySupplierID_Id(Integer supplierId);

    List<Supplierproduct> findBySupplierID_Id(Integer supplierId);

    @Query("SELECT COUNT(DISTINCT sp.supplierID.id) FROM Supplierproduct sp")
    long countDistinctSuppliers();
}
