package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.view.PermissionMatrixCell;
import com.example.project.view.PermissionMatrixRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owner "role-by-branch" permission logic over the existing {@code AccountPermission} table:
 * which account holds which role at which branch. This is the real backend for the
 * {@code /owner/permissions} matrix screen — every operation reads/writes AccountPermission,
 * with no new tables.
 *
 * <p>The screen is a matrix: one row per account, one column per branch, each cell a role
 * dropdown. Rules (validated here, not by a DB constraint): an account holds at most one role
 * per branch, and the {@code OWNER} role can never be assigned, edited or removed through this
 * screen. Friendly Vietnamese messages are thrown as {@link IllegalArgumentException} for the
 * controller to surface as flash messages.</p>
 */
@Service
public class OwnerPermissionService {

    private static final String MSG_MISSING_TARGET = "Thiếu thông tin tài khoản hoặc chi nhánh";
    private static final String MSG_INVALID_ROLE = "Vai trò không hợp lệ";
    private static final String MSG_ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản đã chọn";
    private static final String MSG_BRANCH_NOT_FOUND = "Không tìm thấy chi nhánh đã chọn";
    private static final String MSG_OWNER_LOCKED = "Không thể chỉnh sửa quyền Chủ sở hữu";

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

    /**
     * Builds the permission matrix: one {@link PermissionMatrixRow} per account (optionally
     * filtered by free-text search over name / username / email), each carrying a cell for every
     * branch keyed by branch id.
     */
    @Transactional(readOnly = true)
    public List<PermissionMatrixRow> listMatrixRows(String search, String roleFilter) {
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String normalizedRoleFilter = normalizeRoleFilter(roleFilter);
        List<Branch> branches = listBranches();

        Map<Integer, Map<Integer, Accountpermission>> byAccount = new HashMap<>();
        for (Accountpermission ap : accountpermissionRepository.findAllWithAccountAndBranch()) {
            Account account = ap.getAccountID();
            Branch branch = ap.getBranchID();

            if (account == null || account.getId() == null || branch == null || branch.getId() == null) {
                continue;
            }

            byAccount.computeIfAbsent(account.getId(), key -> new HashMap<>())
                    .putIfAbsent(branch.getId(), ap);
        }

        List<PermissionMatrixRow> rows = new ArrayList<>();

        for (Account account : accountRepository.findAll()) {
            if (!matchesAccount(account, needle)) {
                continue;
            }

            Map<Integer, Accountpermission> branchMap =
                    byAccount.getOrDefault(account.getId(), Map.of());

            boolean ownerAccount = hasRole(branchMap, RoleConstants.OWNER);

            if (!matchesRoleFilter(branchMap, normalizedRoleFilter)) {
                continue;
            }

            Map<Integer, PermissionMatrixCell> cells = new LinkedHashMap<>();

            for (Branch branch : branches) {
                cells.put(branch.getId(),
                        toCell(account.getId(), branch.getId(), branchMap.get(branch.getId())));
            }

            rows.add(new PermissionMatrixRow(
                    account.getId(),
                    account.getName(),
                    usernameOrEmail(account),
                    ownerAccount,
                    cells
            ));
        }

        rows.sort(Comparator
                .comparing(PermissionMatrixRow::isOwnerAccount).reversed()
                .thenComparing(row -> normalizedName(row.getAccountName()))
                .thenComparing(row -> row.getAccountId() == null ? Integer.MAX_VALUE : row.getAccountId()));

        return rows;
    }

    /**
     * Saves one matrix cell (a dropdown change for an account at a branch).
     *
     * <ul>
     *   <li>A blank/empty {@code role} removes the current assignment (the
     *       "-- Chưa phân quyền --" option).</li>
     *   <li>The {@code OWNER} role can never be assigned, and an existing Owner assignment can be
     *       neither changed nor removed here.</li>
     *   <li>Otherwise the role is created (with a manual id — the PK is not AUTO_INCREMENT) or
     *       updated in place, keeping the one-role-per-branch rule.</li>
     * </ul>
     */
    @Transactional
    public void saveMatrixCell(Integer accountId, Integer branchId, String role) {
        if (accountId == null || branchId == null) {
            throw new IllegalArgumentException(MSG_MISSING_TARGET);
        }

        Account account = requireAccount(accountId);
        Branch branch = requireBranch(branchId);

        String normalized = role == null ? "" : canonicalRole(role);
        boolean clearing = normalized == null || normalized.isBlank();

        if (!clearing && isOwnerRole(normalized)) {
            throw new IllegalArgumentException(MSG_OWNER_LOCKED);
        }
        if (!clearing && !RoleConstants.isValid(normalized)) {
            throw new IllegalArgumentException(MSG_INVALID_ROLE);
        }

        Accountpermission existing = findAssignment(accountId, branchId);
        // An Owner assignment is read-only: it can neither be edited nor removed from this screen.
        if (existing != null && isOwnerRole(existing.getRole())) {
            throw new IllegalArgumentException(MSG_OWNER_LOCKED);
        }

        if (clearing) {
            if (existing != null) {
                accountpermissionRepository.delete(existing);
            }
            return;
        }

        if (existing == null) {
            Accountpermission permission = new Accountpermission();
            // Manual id: the PK column is not AUTO_INCREMENT in the schema.
            permission.setId(accountpermissionRepository.findMaxId() + 1);
            permission.setAccountID(account);
            permission.setBranchID(branch);
            permission.setRole(normalized);
            accountpermissionRepository.save(permission);
        } else {
            existing.setRole(normalized);
            accountpermissionRepository.save(existing);
        }
    }

    // ------------------------------------------------------------------

    /** Current assignment for an account at a branch, or {@code null} (one-role-per-branch rule). */
    private Accountpermission findAssignment(Integer accountId, Integer branchId) {
        List<Accountpermission> existing =
                accountpermissionRepository.findByAccountIdAndBranchId(accountId, branchId);
        return existing.isEmpty() ? null : existing.get(0);
    }

    private PermissionMatrixCell toCell(Integer accountId, Integer branchId, Accountpermission ap) {
        if (ap == null) {
            return PermissionMatrixCell.empty(accountId, branchId);
        }
        String role = canonicalRole(ap.getRole());
        return new PermissionMatrixCell(
                ap.getId(), accountId, branchId,
                role, RoleConstants.vietnameseName(role), isOwnerRole(role));
    }

    private Account requireAccount(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_ACCOUNT_NOT_FOUND));
    }

    private Branch requireBranch(Integer branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_BRANCH_NOT_FOUND));
    }

    private boolean isOwnerRole(String role) {
        return role != null && RoleConstants.OWNER.equals(role.trim().toUpperCase(Locale.ROOT));
    }

    private String usernameOrEmail(Account account) {
        if (account.getUsername() != null && !account.getUsername().isBlank()) {
            return account.getUsername();
        }
        return account.getEmail() == null ? "" : account.getEmail();
    }

    private boolean matchesAccount(Account account, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        return contains(account.getName(), needle)
                || contains(account.getUsername(), needle)
                || contains(account.getEmail(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String normalizeRoleFilter(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return null;
        }

        String normalized = canonicalRole(roleFilter);
        return RoleConstants.isValid(normalized) ? normalized : null;
    }

    private boolean matchesRoleFilter(Map<Integer, Accountpermission> branchMap, String roleFilter) {
        if (roleFilter == null) {
            return true;
        }

        return hasRole(branchMap, roleFilter);
    }

    private boolean hasRole(Map<Integer, Accountpermission> branchMap, String role) {
        if (branchMap == null || branchMap.isEmpty() || role == null) {
            return false;
        }

        return branchMap.values().stream()
                .map(Accountpermission::getRole)
                .map(this::canonicalRole)
                .anyMatch(role::equals);
    }

    private String canonicalRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }

    private String normalizedName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
