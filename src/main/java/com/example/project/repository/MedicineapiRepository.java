package com.example.project.repository;

import com.example.project.entity.Medicineapi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** Active ingredients of a single product, for the Product Detail screen. */
    @Query("""
           select m
           from Medicineapi m
           where m.productID.productID = :productId
           order by m.id asc
           """)
    List<Medicineapi> findByProductId(@Param("productId") Integer productId);

    /**
     * Distinct ingredient names already used across products, for the Create Product autocomplete
     * (reuse existing names instead of retyping / risking inconsistent spelling).
     */
    @Query("""
           select distinct m.apiName
           from Medicineapi m
           where m.apiName is not null and m.apiName <> ''
           order by m.apiName asc
           """)
    List<String> findDistinctApiNames();

    /** Distinct strengths already used, for the Create Product autocomplete. */
    @Query("""
           select distinct m.strength
           from Medicineapi m
           where m.strength is not null and m.strength <> ''
           order by m.strength asc
           """)
    List<String> findDistinctStrengths();
}