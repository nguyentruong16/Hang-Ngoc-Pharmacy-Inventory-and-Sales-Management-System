package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.view.PermissionAssignmentRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Owner "role-by-branch" assignment logic over the existing {@code AccountPermission} table:
 * which account has which role at which branch. This is the real backend for the
 * {@code /owner/permissions} screen — every operation reads/writes AccountPermission, with no
 * new tables.
 *
 * <p>Uniqueness rule (validated here, not by a DB constraint): an account may hold at most one
 * role per branch. Inputs are validated and friendly Vietnamese messages are thrown as
 * {@link IllegalArgumentException} for the controller to surface as flash messages.</p>
 */
@Service
public class OwnerPermissionService {

    private static final String MSG_MISSING_FIELDS = "Vui lòng chọn tài khoản, chi nhánh và vai trò";
    private static final String MSG_DUPLICATE = "Tài khoản này đã được phân quyền tại chi nhánh này";
    private static final String MSG_NOT_FOUND = "Không tìm thấy phân quyền cần thao tác";
    private static final String MSG_ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản đã chọn";
    private static final String MSG_BRANCH_NOT_FOUND = "Không tìm thấy chi nhánh đã chọn";

    private final AccountpermissionRepository accountpermissionRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;

    public OwnerPermissionService(AccountpermissionRepository accountpermissionRepository,
                                  AccountRepository accountRepository,
                                  BranchRepository branchRepository) {
        this.accountpermissionRepository = accountpermissionRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
    }

    /** Assignments for the table, optionally filtered by free-text search, branch and role. */
    @Transactional(readOnly = true)
    public List<PermissionAssignmentRow> listAssignments(String search, Integer branchId, String role) {
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String roleFilter = RoleConstants.isValid(role) ? role : null;

        return accountpermissionRepository.findAllWithAccountAndBranch().stream()
                .filter(ap -> matchesSearch(ap, needle))
                .filter(ap -> branchId == null
                        || (ap.getBranchID() != null && branchId.equals(ap.getBranchID().getId())))
                .filter(ap -> roleFilter == null || roleFilter.equals(ap.getRole()))
                .map(this::toRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Account> listAccounts() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(account -> account.getName() == null
                        ? "" : account.getName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Branch> listBranches() {
        return branchRepository.findAll().stream()
                .sorted(Comparator.comparing(branch -> branch.getName() == null
                        ? "" : branch.getName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    @Transactional
    public void create(Integer accountId, Integer branchId, String role) {
        validateInput(accountId, branchId, role);
        if (accountpermissionRepository.existsAssignment(accountId, branchId)) {
            throw new IllegalArgumentException(MSG_DUPLICATE);
        }
        Accountpermission permission = new Accountpermission();
        // Manual id: the PK column is not AUTO_INCREMENT in the schema.
        permission.setId(accountpermissionRepository.findMaxId() + 1);
        permission.setAccountID(requireAccount(accountId));
        permission.setBranchID(requireBranch(branchId));
        permission.setRole(role);
        accountpermissionRepository.save(permission);
    }

    @Transactional
    public void update(Integer id, Integer branchId, String role) {
        validateUpdateInput(branchId, role);

        Accountpermission permission = accountpermissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_NOT_FOUND));

        Account account = permission.getAccountID();
        if (account == null || account.getId() == null) {
            throw new IllegalArgumentException(MSG_ACCOUNT_NOT_FOUND);
        }

        Integer accountId = account.getId();

        if (accountpermissionRepository.existsAssignmentExcludingId(accountId, branchId, id)) {
            throw new IllegalArgumentException(MSG_DUPLICATE);
        }

        permission.setBranchID(requireBranch(branchId));
        permission.setRole(role);
        accountpermissionRepository.save(permission);
    }

    @Transactional
    public void delete(Integer id) {
        if (id == null || !accountpermissionRepository.existsById(id)) {
            throw new IllegalArgumentException(MSG_NOT_FOUND);
        }
        accountpermissionRepository.deleteById(id);
    }

    // ------------------------------------------------------------------

    private void validateUpdateInput(Integer branchId, String role) {
        if (branchId == null || role == null || role.isBlank() || !RoleConstants.isValid(role)) {
            throw new IllegalArgumentException(MSG_MISSING_FIELDS);
        }
    }

    private void validateInput(Integer accountId, Integer branchId, String role) {
        if (accountId == null || branchId == null || role == null || role.isBlank()
                || !RoleConstants.isValid(role)) {
            throw new IllegalArgumentException(MSG_MISSING_FIELDS);
        }
    }

    private Account requireAccount(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_ACCOUNT_NOT_FOUND));
    }

    private Branch requireBranch(Integer branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_BRANCH_NOT_FOUND));
    }

    private boolean matchesSearch(Accountpermission ap, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        Account account = ap.getAccountID();
        if (account == null) {
            return false;
        }
        return contains(account.getName(), needle)
                || contains(account.getUsername(), needle)
                || contains(account.getEmail(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private PermissionAssignmentRow toRow(Accountpermission ap) {
        Account account = ap.getAccountID();
        Branch branch = ap.getBranchID();
        return new PermissionAssignmentRow(
                ap.getId(),
                account == null ? null : account.getId(),
                account == null ? "" : account.getName(),
                account == null ? "" : account.getUsername(),
                account == null ? "" : account.getEmail(),
                branch == null ? null : branch.getId(),
                branch == null ? "" : branch.getName(),
                ap.getRole(),
                RoleConstants.vietnameseName(ap.getRole()));
    }
}
