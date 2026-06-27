package com.example.project.repository;

import com.example.project.entity.Origin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginRepository extends JpaRepository<Origin, Integer> {

    Page<Origin> findByNameContainingIgnoreCase(String name, Pageable pageable);
}