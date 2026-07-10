package com.example.project.repository;

import com.example.project.entity.Stockcount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockcountRepository extends JpaRepository<Stockcount, Integer> {

    @Query("""
           select sc
           from Stockcount sc
           left join fetch sc.createdBy
           left join fetch sc.approvedBy
           order by sc.countDate desc
           """)
    List<Stockcount> findAllWithRelations();

    @Query("""
           select sc
           from Stockcount sc
           left join fetch sc.createdBy
           left join fetch sc.approvedBy
           where sc.id = :id
           """)
    Optional<Stockcount> findByIdWithRelations(@Param("id") Integer id);
}