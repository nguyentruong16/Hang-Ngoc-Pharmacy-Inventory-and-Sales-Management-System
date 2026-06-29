package com.example.project.repository;

import com.example.project.entity.Batch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Integer> {

    /**
     * Current on-hand stock per product = SUM(storageQuantity) across all batches/branches.
     * Each row is {@code [productID (Integer), totalStock (Long)]}. Used for the Owner (all-branch) view.
     */
    @Query("""
           select b.productID.productID, coalesce(sum(b.storageQuantity), 0)
           from Batch b
           where b.productID is not null
           group by b.productID.productID
           """)
    List<Object[]> sumStorageGroupedByProduct();

    /** Same as {@link #sumStorageGroupedByProduct()} but scoped to a single branch. */
    @Query("""
       select b.productID.productID, coalesce(sum(b.storageQuantity), 0)
       from Batch b
       where b.productID is not null
         and b.branchID.id = :branchId
       group by b.productID.productID
       """)
    List<Object[]> sumStorageGroupedByProductAndBranch(@Param("branchId") Integer branchId);

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

    /**
     * On-hand stock of one product grouped by branch, scoped to {@code branchId} when given
     * ({@code null} = all branches, for the Owner). Each row is
     * {@code [branchID (Integer), branchName (String), totalStock (Long)]}.
     */
    @Query("""
       select b.branchID.id, b.branchID.name, coalesce(sum(b.storageQuantity), 0)
       from Batch b
       where b.productID.productID = :productId
         and (:branchId is null or b.branchID.id = :branchId)
       group by b.branchID.id, b.branchID.name
       order by b.branchID.name asc
       """)
    List<Object[]> sumStorageByProductGroupedByBranch(@Param("productId") Integer productId,
                                                       @Param("branchId") Integer branchId);

    /** In-stock (storageQuantity &gt; 0, active) batches of one product, branch-scoped, soonest-expiry first. */
    @Query("""
       select b
       from Batch b
       left join fetch b.branchID
       left join fetch b.importUnitID
       where b.productID.productID = :productId
         and b.storageQuantity > 0
         and b.status = true
         and (:branchId is null or b.branchID.id = :branchId)
       order by b.expirationDate asc
       """)
    List<Batch> findInStockBatchesByProduct(@Param("productId") Integer productId,
                                            @Param("branchId") Integer branchId);

    /** Most recent import (batch) events of one product, branch-scoped, for the history preview. */
    @Query("""
       select b
       from Batch b
       left join fetch b.branchID
       where b.productID.productID = :productId
         and (:branchId is null or b.branchID.id = :branchId)
       order by b.importDate desc
       """)
    List<Batch> findRecentImportsByProduct(@Param("productId") Integer productId,
                                          @Param("branchId") Integer branchId,
                                          Pageable pageable);
}