package com.example.project.repository;

import com.example.project.entity.Shiftreport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShiftreportRepository extends JpaRepository<Shiftreport, Integer> {

    @Query("""
           select s
           from Shiftreport s
           left join fetch s.cashierID
           order by s.startTime desc
           """)
    List<Shiftreport> findAllWithRelations();

    @Query("""
           select s
           from Shiftreport s
           left join fetch s.cashierID
           where s.id = :id
           """)
    Optional<Shiftreport> findByIdWithRelations(@Param("id") Integer id);

    /** Open (Nháp) shift of an account, if any — used by ensureOpenShiftFor / logout guard. */
    Optional<Shiftreport> findFirstByCashierID_IdAndStatusOrderByStartTimeDesc(Integer cashierId, String status);

    /** Most recently approved shift of an account — used to inherit openingCash. */
    Optional<Shiftreport> findFirstByCashierID_IdAndStatusOrderByApprovedAtDesc(Integer cashierId, String status);
}
