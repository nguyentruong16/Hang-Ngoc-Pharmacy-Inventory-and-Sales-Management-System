package com.example.project.repository;

import com.example.project.entity.Medicineapi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedicineapiRepository extends JpaRepository<Medicineapi, Integer> {

    /**
     * All active pharmaceutical ingredients with their owning product eagerly loaded,
     * used by the product list to show the ingredient(s) of each product.
     */
    @Query("""
           select m
           from Medicineapi m
           left join fetch m.productID
           where m.productID is not null
           """)
    List<Medicineapi> findAllWithProduct();
}