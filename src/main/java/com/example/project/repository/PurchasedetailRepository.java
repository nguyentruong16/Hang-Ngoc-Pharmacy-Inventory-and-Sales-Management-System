package com.example.project.repository;

import com.example.project.entity.Purchasedetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchasedetailRepository extends JpaRepository<Purchasedetail, Integer> {

    @Query("""
           select d
           from Purchasedetail d
           left join fetch d.purchaseID
           left join fetch d.productID
           where d.purchaseID.id = :purchaseId
           order by d.id asc
           """)
    List<Purchasedetail> findByPurchaseIdWithProduct(Integer purchaseId);

    @Query("""
           select d
           from Purchasedetail d
           left join fetch d.purchaseID
           left join fetch d.productID
           """)
    List<Purchasedetail> findAllWithRelations();
}