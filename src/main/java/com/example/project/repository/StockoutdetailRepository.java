package com.example.project.repository;

import com.example.project.entity.Stockoutdetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockoutdetailRepository extends JpaRepository<Stockoutdetail, Integer> {

    @Query("""
           select d
           from Stockoutdetail d
           left join fetch d.stockOutID
           left join fetch d.productID
           left join fetch d.productUnitID
           left join fetch d.batchID
           where d.stockOutID.id = :stockOutId
           order by d.id asc
           """)
    List<Stockoutdetail> findByStockOutIdWithRelations(Integer stockOutId);

    @Query("""
           select d
           from Stockoutdetail d
           left join fetch d.stockOutID
           left join fetch d.productID
           left join fetch d.productUnitID
           left join fetch d.batchID
           """)
    List<Stockoutdetail> findAllWithRelations();
}