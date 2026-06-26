package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.entity.Branch;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.repository.BranchRepository;
import com.example.project.view.BranchPermissionRow;
import com.example.project.view.PermissionBranchOption;
import com.example.project.view.PermissionPageView;
import com.example.project.security.AccountPrincipal;
import com.example.project.view.PermissionMatrixCell;
import com.example.project.view.PermissionMatrixRow;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Owner "role-by-branch" permission logic over the existing {@code AccountPermission} table:
 * which account holds which role at which branch. This is the real backend for the
 * {@code /owner/permissions} screen — every operation reads/writes AccountPermission, with no
 * new tables.
 *
 * <p>The screen is branch-selected and account-paginated: the Owner picks one branch (via the
 * branch buttons) and sees a paged table of accounts with their role <em>at that branch</em>.
 * Rules, validated here rather than by a DB constraint:</p>
 * <ul>
 *   <li>An account holds at most one role per branch.</li>
 *   <li>The {@code OWNER} role can never be assigned, edited or removed through this screen, and
 *       the Owner stays Owner at every branch.</li>
 *   <li>An <em>inactive</em> branch (status != {@code "Đang hoạt động"}) is read-only — no role
 *       change is accepted for it.</li>
 * </ul>
 * Friendly Vietnamese messages are thrown as {@link IllegalArgumentException} for the controller
 * to surface as flash messages.
 */
@Service
public class OwnerPermissionService {

    private static final String ACTIVE_STATUS_NAME = "Đang hoạt động";
    private static final String INACTIVE_STATUS_LABEL = "Đã ngừng hoạt động";
    private static final int DEFAULT_SIZE = 10;

    private static final String MSG_MISSING_TARGET = "Thiếu thông tin tài khoản hoặc chi nhánh";
    private static final String MSG_INVALID_ROLE = "Vai trò không hợp lệ";
    private static final String MSG_ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản đã chọn";
    private static final String MSG_BRANCH_NOT_FOUND = "Không tìm thấy chi nhánh đã chọn";
    private static final String MSG_OWNER_LOCKED = "Không thể chỉnh sửa quyền Chủ sở hữu";
    private static final String MSG_BRANCH_INACTIVE =
            "Chi nhánh đã ngừng hoạt động, không thể thay đổi phân quyền";
    private static final String MSG_CHIEF_PHARMACIST_LIMIT =
            "Mỗi chi nhánh chỉ được có một Dược sĩ trưởng";

    private final AccountpermissionRepository accountpermissionRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final SessionRegistry sessionRegistry;

    public OwnerPermissionService(AccountpermissionRepository accountpermissionRepository,
                                  AccountRepository accountRepository,
                                  BranchRepository branchRepository,
                                  SessionRegistry sessionRegistry) {
        this.accountpermissionRepository = accountpermissionRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Builds the whole screen state for one render: the branch buttons, the resolved selected
     * branch, and one page of account rows for that branch.
     *
     * <ul>
     *   <li>When {@code branchId} is null or unknown, the first branch (sorted by name) is
     *       selected; when there are no branches at all the page is empty.</li>
     *   <li>{@code search} matches account name / username / email; {@code roleFilter} keeps only
     *       accounts whose role <em>at the selected branch</em> equals the filter.</li>
     *   <li>Pagination is by account row ({@code page} is 0-based, {@code size} defaults to 10).</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public PermissionPageView getPermissionPage(Integer branchId, String search,
                                                String roleFilter, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? DEFAULT_SIZE : size;
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String normalizedRoleFilter = normalizeRoleFilter(roleFilter);

        // Branches sorted by name; status eagerly fetched so active/inactive is safe to read.
        List<Branch> branches = branchRepository.findAllWithStatus().stream()
                .sorted(Comparator.comparing(branch -> branch.getName() == null
                        ? "" : branch.getName().toLowerCase(Locale.ROOT)))
                .toList();

        Branch selected = resolveSelectedBranch(branches, branchId);

        List<PermissionBranchOption> branchOptions = branches.stream()
                .map(branch -> toBranchOption(branch, selected))
                .toList();

        PermissionBranchOption selectedOption = branchOptions.stream()
                .filter(PermissionBranchOption::isSelected)
                .findFirst()
                .orElse(null);

        // No branches at all → empty state.
        if (selected == null) {
            return new PermissionPageView(branchOptions, null, List.of(),
                    0, safeSize, 0L, 0, search, roleFilter);
        }

        boolean branchActive = isActiveBranch(selected);

        // accountId -> the account's assignment at the selected branch (one-role-per-branch).
        Map<Integer, Accountpermission> assignmentByAccount = new HashMap<>();
        for (Accountpermission ap : accountpermissionRepository.findAllWithAccountAndBranch()) {
            Account account = ap.getAccountID();
            Branch branch = ap.getBranchID();
            if (account == null || account.getId() == null || branch == null || branch.getId() == null) {
                continue;
            }
            if (selected.getId().equals(branch.getId())) {
                assignmentByAccount.putIfAbsent(account.getId(), ap);
            }
        }

        List<BranchPermissionRow> allRows = new ArrayList<>();
        for (Account account : accountRepository.findAll()) {
            if (account.getId() == null || !matchesAccount(account, needle)) {
                continue;
            }

            Accountpermission ap = assignmentByAccount.get(account.getId());
            String role = ap == null ? null : canonicalRole(ap.getRole());

            if (!matchesRoleFilter(role, normalizedRoleFilter)) {
                continue;
            }

            boolean ownerLocked = isOwnerRole(role);
            // Editable only when the branch is active and the row is not the Owner.
            boolean editable = branchActive && !ownerLocked;
            String roleDisplay = (role == null || role.isBlank())
                    ? "Chưa phân quyền"
                    : RoleConstants.vietnameseName(role);

            allRows.add(new BranchPermissionRow(
                    account.getId(), account.getName(), usernameOrEmail(account),
                    role, roleDisplay, ownerLocked, editable));
        }

        // Owner first, then by account name, then by id.
        allRows.sort(Comparator
                .comparing(BranchPermissionRow::isOwnerLocked).reversed()
                .thenComparing(row -> normalizedName(row.getAccountName()))
                .thenComparing(row -> row.getAccountId() == null ? Integer.MAX_VALUE : row.getAccountId()));

        // In-memory pagination by account row (the account set is small).
        long totalElements = allRows.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int boundedPage = totalPages == 0 ? 0 : Math.min(safePage, totalPages - 1);
        int fromIndex = boundedPage * safeSize;
        List<BranchPermissionRow> pageRows = fromIndex >= allRows.size()
                ? List.of()
                : List.copyOf(allRows.subList(fromIndex, Math.min(fromIndex + safeSize, allRows.size())));

        return new PermissionPageView(branchOptions, selectedOption, pageRows,
                boundedPage, safeSize, totalElements, totalPages, search, roleFilter);
    }

    /**
     * Saves one account's role at one branch (a dropdown change in the per-branch table).
     *
     * <ul>
     *   <li>An inactive branch is read-only — the update is rejected up front.</li>
     *   <li>A blank/empty {@code role} removes the current assignment ("Chưa phân quyền").</li>
     *   <li>The {@code OWNER} role can never be assigned, and an existing Owner assignment can be
     *       neither changed nor removed here.</li>
     *   <li>Otherwise the role is created (with a manual id — the PK is not AUTO_INCREMENT) or
     *       updated in place, keeping the one-role-per-branch rule.</li>
     * </ul>
     */
    @Transactional
    public void saveBranchRole(Integer accountId, Integer branchId, String role) {
        if (accountId == null || branchId == null) {
            throw new IllegalArgumentException(MSG_MISSING_TARGET);
        }

        Account account = requireAccount(accountId);
        Branch branch = requireBranch(branchId);

        // Task 2: never trust the UI — reject any change to an inactive branch server-side.
        if (!isActiveBranch(branch)) {
            throw new IllegalArgumentException(MSG_BRANCH_INACTIVE);
        }

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

        // Each branch can have at most one Chief Pharmacist.
        if (!clearing
                && isChiefPharmacistRole(normalized)
                && accountpermissionRepository.existsChiefPharmacistInBranchExcludingAccount(branchId, accountId)) {
            throw new IllegalArgumentException(MSG_CHIEF_PHARMACIST_LIMIT);
        }

        if (clearing) {
            if (existing != null) {
                accountpermissionRepository.delete(existing);
                invalidateSessionsAt(accountId, branchId);
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
        invalidateSessionsAt(accountId, branchId);
    }

    private void invalidateSessionsAt(Integer accountId, Integer branchId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof AccountPrincipal ap
                        && ap.getAccountId().equals(accountId)
                        && Objects.equals(ap.getBranchId(), branchId))
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .forEach(SessionInformation::expireNow);
    }

    // ------------------------------------------------------------------

    /** Requested branch if it exists in the list, otherwise the first (sorted) branch, or null. */
    private Branch resolveSelectedBranch(List<Branch> branches, Integer branchId) {
        if (branches.isEmpty()) {
            return null;
        }
        if (branchId != null) {
            for (Branch branch : branches) {
                if (branchId.equals(branch.getId())) {
                    return branch;
                }
            }
        }
        return branches.get(0);
    }

    private PermissionBranchOption toBranchOption(Branch branch, Branch selected) {
        boolean active = isActiveBranch(branch);
        String statusName = branch.getStatusID() == null ? null : branch.getStatusID().getName();
        boolean isSelected = selected != null && selected.getId() != null
                && selected.getId().equals(branch.getId());
        String displayStatus = active ? ACTIVE_STATUS_NAME : INACTIVE_STATUS_LABEL;
        return new PermissionBranchOption(branch.getId(), branch.getName(), statusName,
                active, isSelected, displayStatus);
    }

    /** A branch is active only when its status is exactly {@code "Đang hoạt động"}. */
    private boolean isActiveBranch(Branch branch) {
        return branch != null && branch.getStatusID() != null
                && ACTIVE_STATUS_NAME.equals(branch.getStatusID().getName());
    }

    /** Current assignment for an account at a branch, or {@code null} (one-role-per-branch rule). */
    private Accountpermission findAssignment(Integer accountId, Integer branchId) {
        List<Accountpermission> existing =
                accountpermissionRepository.findByAccountIdAndBranchId(accountId, branchId);
        return existing.isEmpty() ? null : existing.get(0);
    }

    private Account requireAccount(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_ACCOUNT_NOT_FOUND));
    }

    private Branch requireBranch(Integer branchId) {
        // Fetch with status so the inactive-branch check never trips a lazy-load.
        return branchRepository.findByIdWithStatus(branchId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_BRANCH_NOT_FOUND));
    }

    private boolean isOwnerRole(String role) {
        return role != null && RoleConstants.OWNER.equals(role.trim().toUpperCase(Locale.ROOT));
    }

    private boolean isChiefPharmacistRole(String role) {
        return role != null
                && RoleConstants.CHIEF_PHARMACIST.equals(role.trim().toUpperCase(Locale.ROOT));
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

    /** Keep a row only when its role at the selected branch matches the active filter. */
    private boolean matchesRoleFilter(String role, String roleFilter) {
        return roleFilter == null || roleFilter.equals(role);
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
