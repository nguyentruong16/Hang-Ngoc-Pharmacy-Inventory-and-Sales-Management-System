package com.example.project.repository;

import com.example.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Loads every product together with the relations needed by the product list
     * (type / producer) so the listing screen avoids lazy N+1 lookups.
     */
    @Query("""
       select distinct p
       from Product p
       left join fetch p.typeID
       left join fetch p.producerID
       order by p.name asc
       """)
    List<Product> findAllWithRelations();

    /** Loads a single product with the relations needed by the Product Detail header. */
    @Query("""
       select p
       from Product p
       left join fetch p.typeID
       left join fetch p.producerID
       left join fetch p.originID
       where p.productID = :productId
       """)
    Optional<Product> findDetailById(@Param("productId") Integer productId);

    /** Product code is unique; used to reject duplicates when creating a product. */
    boolean existsByCode(String code);

    /** Barcode is unique when present; used to reject duplicates when creating a product. */
    boolean existsByBarcode(String barcode);
}