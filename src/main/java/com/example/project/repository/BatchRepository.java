package com.example.project.repository;

import com.example.project.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Integer> {

    /**
     * Current on-hand stock per product = SUM(storageQuantity) across all batches/branches.
     * Each row is {@code [productID (String), totalStock (Long)]}.
     */
    @Query("""
           select b.productID.productID, coalesce(sum(b.storageQuantity), 0)
           from Batch b
           where b.productID is not null
           group by b.productID.productID
           """)
    List<Object[]> sumStorageGroupedByProduct();
}