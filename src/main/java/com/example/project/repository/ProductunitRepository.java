package com.example.project.repository;

import com.example.project.entity.Productunit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductunitRepository extends JpaRepository<Productunit, Integer> {

    @Query("""
           select u
           from Productunit u
           left join fetch u.productID
           where u.productID is not null
           """)
    List<Productunit> findAllWithProduct();

    @Query("""
           select u
           from Productunit u
           left join fetch u.productID
           where u.productID.productID = :productId
           """)
    List<Productunit> findByProductId(@Param("productId") Integer productId);
}