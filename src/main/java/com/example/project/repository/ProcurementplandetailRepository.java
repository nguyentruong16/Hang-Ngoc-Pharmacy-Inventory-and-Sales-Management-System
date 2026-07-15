package com.example.project.repository;

import com.example.project.entity.Procurementplandetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcurementplandetailRepository extends JpaRepository<Procurementplandetail, Integer> {

    List<Procurementplandetail> findByProcurementID_Id(Integer procurementId);

    @Query("""
            SELECT d FROM Procurementplandetail d
            JOIN FETCH d.productID
            LEFT JOIN FETCH d.supplierID
            WHERE d.procurementID.id = :procurementId
            """)
    List<Procurementplandetail> findByProcurementID_IdWithRelations(@Param("procurementId") Integer procurementId);

    void deleteByProcurementID_Id(Integer procurementId);
}