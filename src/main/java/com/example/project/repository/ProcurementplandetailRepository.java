package com.example.project.repository;

import com.example.project.entity.Procurementplandetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementplandetailRepository extends JpaRepository<Procurementplandetail, Integer> {

    List<Procurementplandetail> findByProcurementID_Id(Integer procurementId);

    void deleteByProcurementID_Id(Integer procurementId);
}