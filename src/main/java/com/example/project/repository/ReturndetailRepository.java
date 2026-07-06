package com.example.project.repository;

import com.example.project.entity.Returndetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturndetailRepository extends JpaRepository<Returndetail, Integer> {

    /** Most recent return lines of one product, for the Product Detail history preview. */
    @Query("""
           select d
           from Returndetail d
           left join fetch d.returnID
           left join fetch d.batchID
           where d.productID.productID = :productId
           order by d.returnID.returnDate desc
           """)
    List<Returndetail> findRecentReturnsByProduct(@Param("productId") Integer productId,
                                                  Pageable pageable);
}