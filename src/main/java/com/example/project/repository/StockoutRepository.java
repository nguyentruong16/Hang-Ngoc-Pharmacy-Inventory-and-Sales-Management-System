package com.example.project.repository;

import com.example.project.entity.Stockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface StockoutRepository extends JpaRepository<Stockout, Integer> {

    @Query("""
           select s
           from Stockout s
           left join fetch s.createdBy
           left join fetch s.approvedBy
           left join fetch s.expenseID
           order by s.date desc
           """)
    List<Stockout> findAllWithRelations();

    @Query("""
           select s
           from Stockout s
           left join fetch s.createdBy
           left join fetch s.approvedBy
           left join fetch s.expenseID
           where s.id = :id
           """)
    Optional<Stockout> findByIdWithRelations(Integer id);
}