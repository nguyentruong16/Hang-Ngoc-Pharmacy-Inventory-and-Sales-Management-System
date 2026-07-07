package com.example.project.repository;

import com.example.project.entity.Stockadjustmentdetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockadjustmentdetailRepository extends JpaRepository<Stockadjustmentdetail, Integer> {

    @Query("""
           select d
           from Stockadjustmentdetail d
           left join fetch d.stockAdjustmentID
           left join fetch d.productID
           left join fetch d.productUnitID
           left join fetch d.batchID
           where d.stockAdjustmentID.id = :stockOutId
           order by d.id asc
           """)
    List<Stockadjustmentdetail> findByStockOutIdWithRelations(Integer stockOutId);

    /** Most recent stock-out lines of one product, for the Product Detail history preview. */
    @Query("""
       select d
       from Stockadjustmentdetail d
       left join fetch d.stockAdjustmentID
       left join fetch d.batchID
       where d.productID.productID = :productId
       order by d.stockAdjustmentID.date desc
       """)
    List<Stockadjustmentdetail> findRecentStockOutsByProduct(@Param("productId") Integer productId,
                                                             Pageable pageable);

    @Query("""
           select d
           from Stockadjustmentdetail d
           left join fetch d.stockAdjustmentID
           left join fetch d.productID
           left join fetch d.productUnitID
           left join fetch d.batchID
           """)
    List<Stockadjustmentdetail> findAllWithRelations();
}