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

    /**
     * All assignments with account eagerly fetched, for the single-store (flat account-role)
     * Permission Table. Unlike {@link #findAllWithAccountAndBranch()} this does not touch
     * {@code branchID} at all.
     */
    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.accountID
           order by ap.id
           """)
    List<Accountpermission> findAllWithAccount();

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

    /**
     * True when the branch already has a Chief Pharmacist assigned to another account.
     * Used by the Owner Permission Table to enforce:
     * one CHIEF_PHARMACIST per branch.
     */
    @Query("""
       select count(ap) > 0
       from Accountpermission ap
       where ap.branchID.id = :branchId
         and upper(ap.role) = 'CHIEF_PHARMACIST'
         and ap.accountID.id <> :accountId
       """)
    boolean existsChiefPharmacistInBranchExcludingAccount(@Param("branchId") Integer branchId,
                                                          @Param("accountId") Integer accountId);

    /** Largest existing id (0 when the table is empty); used to assign the next id. */
    @Query("select coalesce(max(ap.id), 0) from Accountpermission ap")
    Integer findMaxId();

    /**
     * Every {@code OWNER} assignment, account eagerly fetched, ordered by id. Used to locate the
     * single system Owner account so it can be auto-assigned to newly created branches.
     */
    @Query("""
           select ap
           from Accountpermission ap
           left join fetch ap.accountID
           where upper(ap.role) = 'OWNER'
           order by ap.id
           """)
    List<Accountpermission> findOwnerAssignments();

    /** The first (lowest-id) Owner assignment, i.e. the system Owner account, if one exists. */
    default Optional<Accountpermission> findFirstOwnerPermission() {
        return findOwnerAssignments().stream().findFirst();
    }

    /**
     * Every assignment whose role is {@code OWNER} or the legacy {@code CHIEF_PHARMACIST} (merged
     * into {@code OWNER} under the single-store role model). Used by the Permission Table to make
     * sure a save never leaves the system with zero owners.
     */
    @Query("""
           select ap
           from Accountpermission ap
           where upper(ap.role) = 'OWNER' or upper(ap.role) = 'CHIEF_PHARMACIST'
           """)
    List<Accountpermission> findOwnerLikeAssignments();
}
