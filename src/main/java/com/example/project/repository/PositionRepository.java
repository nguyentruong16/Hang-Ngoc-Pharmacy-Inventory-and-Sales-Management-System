package com.example.project.repository;

import com.example.project.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PositionRepository extends JpaRepository<Position, Integer> {

    @Query("""
            SELECT p FROM Position p
            JOIN p.productID prod
            LEFT JOIN p.branchID br
            WHERE (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(prod.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(prod.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:branchId IS NULL OR br.id = :branchId)
            """)
    Page<Position> findFiltered(@Param("keyword") String keyword,
                                @Param("branchId") Integer branchId,
                                Pageable pageable);
}
