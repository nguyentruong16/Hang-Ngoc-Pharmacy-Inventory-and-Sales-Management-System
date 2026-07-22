package com.example.project.repository;

import com.example.project.entity.Invoicedetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoicedetailRepository extends JpaRepository<Invoicedetail, Integer> {

    /** Most recent sale lines of one product, for the Product Detail history preview. */
    @Query("""
       select d
       from Invoicedetail d
       left join fetch d.invoiceID
       left join fetch d.batchID
       where d.productID.productID = :productId
       order by d.invoiceID.date desc
       """)
    List<Invoicedetail> findRecentSalesByProduct(@Param("productId") Integer productId,
                                                 Pageable pageable);

    @Query("""
       select d
       from Invoicedetail d
       left join fetch d.productID
       left join fetch d.productUnitID
       left join fetch d.batchID b
       left join fetch b.importUnitID
       where d.invoiceID.id = :invoiceId
       order by d.id
       """)
    List<Invoicedetail> findByInvoiceIdWithRelations(@Param("invoiceId") Integer invoiceId);

    @Query("""
       select d.invoiceID.id, sum(d.quantity), sum(coalesce(d.returnedQty, 0))
       from Invoicedetail d
       group by d.invoiceID.id
       """)
    List<Object[]> sumQuantitiesGroupedByInvoice();
}