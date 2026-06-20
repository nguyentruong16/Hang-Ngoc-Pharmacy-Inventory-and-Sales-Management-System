package com.example.project.repository;

import com.example.project.entity.Accountpermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountpermissionRepository extends JpaRepository<Accountpermission, Integer> {

    @Query("select permission from Accountpermission permission where permission.accountID.id = :accountId")
    List<Accountpermission> findByAccountId(@Param("accountId") Integer accountId);

    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.branchID
           where ap.accountID.id = :accountId and ap.branchID.id = :branchId
           order by ap.id
           """)
    List<Accountpermission> findByAccountIdAndBranchId(@Param("accountId") Integer accountId,
                                                       @Param("branchId") Integer branchId);

    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.branchID
           where ap.accountID.id = :accountId
           """)
    List<Accountpermission> findProfilePermissionsByAccountId(@Param("accountId") Integer accountId);

    /** All assignments with account + branch eagerly fetched, for the Owner permission table. */
    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.accountID
           left join fetch ap.branchID
           order by ap.id
           """)
    List<Accountpermission> findAllWithAccountAndBranch();

    /** True if the account already has any role assignment at the branch. */
    @Query("""
           select count(ap) > 0 from Accountpermission ap
           where ap.accountID.id = :accountId and ap.branchID.id = :branchId
           """)
    boolean existsAssignment(@Param("accountId") Integer accountId, @Param("branchId") Integer branchId);

    /** Same as {@link #existsAssignment} but ignores the row being edited. */
    @Query("""
           select count(ap) > 0 from Accountpermission ap
           where ap.accountID.id = :accountId and ap.branchID.id = :branchId and ap.id <> :excludeId
           """)
    boolean existsAssignmentExcludingId(@Param("accountId") Integer accountId,
                                        @Param("branchId") Integer branchId,
                                        @Param("excludeId") Integer excludeId);

    /** Largest existing id (0 when the table is empty); used to assign the next id. */
    @Query("select coalesce(max(ap.id), 0) from Accountpermission ap")
    Integer findMaxId();
}
