package com.example.project.repository;

import com.example.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Loads every product together with the relations needed by the product list
     * (type / producer / base unit) so the listing screen avoids lazy N+1 lookups.
     */
    @Query("""
       select distinct p
       from Product p
       left join fetch p.typeID
       left join fetch p.producerID
       order by p.name asc
       """)
    List<Product> findAllWithRelations();
}