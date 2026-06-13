package com.example.project.repository;

import com.example.project.entity.Accountpermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountpermissionRepository extends JpaRepository<Accountpermission, Integer> {

    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.branchID
           where ap.accountID.id = :accountId
           """)
    Optional<Accountpermission> findProfilePermissionByAccountId(@Param("accountId") Integer accountId);
}