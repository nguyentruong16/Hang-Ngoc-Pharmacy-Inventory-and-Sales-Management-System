package com.example.project.repository;

import com.example.project.entity.Stockcountdetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockcountdetailRepository extends JpaRepository<Stockcountdetail, Integer> {

    @Query("""
           select d
           from Stockcountdetail d
           left join fetch d.stockCountID
           left join fetch d.productID
           left join fetch d.batchID
           where d.stockCountID.id = :stockCountId
           order by d.id asc
           """)
    List<Stockcountdetail> findByStockCountIdWithRelations(@Param("stockCountId") Integer stockCountId);

    @Query("""
           select d
           from Stockcountdetail d
           left join fetch d.stockCountID
           left join fetch d.productID
           left join fetch d.batchID
           order by d.id asc
           """)
    List<Stockcountdetail> findAllWithRelations();
}