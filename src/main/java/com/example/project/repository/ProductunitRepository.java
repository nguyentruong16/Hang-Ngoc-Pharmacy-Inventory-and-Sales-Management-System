package com.example.project.repository;

import com.example.project.entity.Productunit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductunitRepository extends JpaRepository<Productunit, Integer> {

    /**
     * All product units with their owning product eagerly loaded, used by the product list
     * to resolve the main selling unit and its sell price per product.
     */
    @Query("""
           select u
           from Productunit u
           left join fetch u.productID
           where u.productID is not null
           """)
    List<Productunit> findAllWithProduct();
}