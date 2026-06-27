package com.example.project.repository;

import com.example.project.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Integer> {

    @Query("""
           select count(b) > 0
           from Batch b
           where b.purchaseDetailID.id = :purchaseDetailId
           """)
    boolean existsByPurchaseDetailId(@Param("purchaseDetailId") Integer purchaseDetailId);

    @Query("""
           select b
           from Batch b
           left join fetch b.purchaseDetailID
           where b.purchaseDetailID.id in :purchaseDetailIds
           """)
    List<Batch> findByPurchaseDetailIds(@Param("purchaseDetailIds") List<Integer> purchaseDetailIds);
}