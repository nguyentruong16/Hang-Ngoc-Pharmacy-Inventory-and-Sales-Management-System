package com.example.project.repository;

import com.example.project.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
       select b
       from Batch b
       left join fetch b.productID
       left join fetch b.branchID
       left join fetch b.importUnitID
       where b.status = true
       and b.storageQuantity > 0
       order by b.expirationDate asc
       """)
    List<Batch> findAvailableBatchesForDestroy();
}