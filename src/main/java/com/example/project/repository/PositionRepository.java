package com.example.project.repository;

import com.example.project.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Integer> {

    /** Storage-location rows of a single product, for the Product Detail/Edit screens. */
    @Query("""
            SELECT p FROM Position p
            WHERE p.productID.productID = :productId
            ORDER BY p.id ASC
            """)
    List<Position> findByProductId(@Param("productId") Integer productId);

    @Query("""
            SELECT p FROM Position p
            JOIN p.productID prod
            WHERE (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(prod.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(prod.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Position> findFiltered(@Param("keyword") String keyword, Pageable pageable);
}
