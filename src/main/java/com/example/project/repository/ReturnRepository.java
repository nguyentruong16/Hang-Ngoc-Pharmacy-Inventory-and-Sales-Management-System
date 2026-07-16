package com.example.project.repository;

import com.example.project.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReturnRepository extends JpaRepository<Return, Integer> {

    /** All returns with their invoice / customer / creator, for the list screen. */
    @Query("""
       select r
       from Return r
       left join fetch r.invoiceID inv
       left join fetch inv.customerID
       left join fetch r.returnedBy
       """)
    List<Return> findAllWithRelations();

    /** One return with its invoice / customer / creator, for the detail screen. */
    @Query("""
       select r
       from Return r
       left join fetch r.invoiceID inv
       left join fetch inv.customerID
       left join fetch r.returnedBy
       where r.id = :id
       """)
    Optional<Return> findByIdWithRelations(@Param("id") Integer id);

    /** Customer returns for a sale invoice, newest first. */
    List<Return> findByInvoiceID_IdOrderByReturnDateDesc(Integer invoiceId);
}
