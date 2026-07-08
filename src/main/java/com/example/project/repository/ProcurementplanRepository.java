package com.example.project.repository;

import com.example.project.entity.Procurementplan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementplanRepository extends JpaRepository<Procurementplan, Integer> {

    Page<Procurementplan> findByProcurementCodeContainingIgnoreCase(String procurementCode, Pageable pageable);
}