package com.example.project.repository;

import com.example.project.entity.Invoicedetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoicedetailRepository extends JpaRepository<Invoicedetail, Integer> {

    /** Most recent sale lines of one product, branch-scoped, for the Product Detail history preview. */
    @Query("""
           select d
           from Invoicedetail d
           left join fetch d.invoiceID
           left join fetch d.batchID
           where d.productID.productID = :productId
             and (:branchId is null or d.invoiceID.branchID.id = :branchId)
           order by d.invoiceID.date desc
           """)
    List<Invoicedetail> findRecentSalesByProduct(@Param("productId") Integer productId,
                                                 @Param("branchId") Integer branchId,
                                                 Pageable pageable);
}