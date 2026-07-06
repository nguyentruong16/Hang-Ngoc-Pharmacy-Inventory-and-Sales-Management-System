package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.entity.Account;
import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.AccountpermissionRepository;
import com.example.project.view.PermissionAccountRow;
import com.example.project.view.PermissionPageView;
import com.example.project.security.AccountPrincipal;
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

/**
 * Owner "account &rarr; role" permission logic over the existing {@code AccountPermission} table,
 * for the single-store {@code /owner/permissions} screen: which account holds which role,
 * system-wide. There are no branches any more — every operation reads/writes
 * {@code AccountPermission} directly, with no new tables.
 *
 * <p>Rules, validated here rather than by a DB constraint:</p>
 * <ul>
 *   <li>An account holds at most one role (one {@code AccountPermission} row).</li>
 *   <li>Assignable roles are exactly {@code PHARMACIST}, {@code ACCOUNTANT}, {@code OWNER}, plus
 *       blank/"Không quyền" to clear the assignment. The legacy {@code CHIEF_PHARMACIST} role can
 *       no longer be assigned from this screen — it has been merged into {@code OWNER} ("Chủ nhà
 *       thuốc"); any existing {@code CHIEF_PHARMACIST} row is displayed and counted as an Owner
 *       (see {@link #canonicalDisplayRole}) but is never written back as-is.</li>
 *   <li>The system must always keep at least one Owner: an account that currently holds an
 *       owner-like role cannot be changed away from it while it is the last one.</li>
 * </ul>
 * Friendly Vietnamese messages are thrown as {@link IllegalArgumentException} for the controller
 * to surface as flash messages.
 */
@Service
public class OwnerPermissionService {

    private static final int DEFAULT_SIZE = 10;

    private static final String MSG_MISSING_ACCOUNT = "Thiếu thông tin tài khoản";
    private static final String MSG_INVALID_ROLE = "Vai trò không hợp lệ";
    private static final String MSG_ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản đã chọn";
    private static final String MSG_LAST_OWNER =
            "Không thể thay đổi vì đây là Chủ nhà thuốc duy nhất trong hệ thống";

    private final AccountpermissionRepository accountpermissionRepository;
    private final AccountRepository accountRepository;
    private final SessionRegistry sessionRegistry;

    public OwnerPermissionService(AccountpermissionRepository accountpermissionRepository,
                                  AccountRepository accountRepository,
                                  SessionRegistry sessionRegistry) {
        this.accountpermissionRepository = accountpermissionRepository;
        this.accountRepository = accountRepository;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Builds the whole screen state for one render: one page of account rows, each with its
     * system-wide role.
     *
     * <ul>
     *   <li>{@code search} matches account name / username / email.</li>
     *   <li>Pagination is by account row ({@code page} is 0-based, {@code size} defaults to 10).</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public PermissionPageView getPermissionPage(String search, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? DEFAULT_SIZE : size;
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        // accountId -> its single assignment (one-role-per-account).
        Map<Integer, Accountpermission> assignmentByAccount = new HashMap<>();
        for (Accountpermission ap : accountpermissionRepository.findAllWithAccount()) {
            Account account = ap.getAccountID();
            if (account == null || account.getId() == null) {
                continue;
            }
            assignmentByAccount.putIfAbsent(account.getId(), ap);
        }

        int ownerLikeCount = (int) assignmentByAccount.values().stream()
                .filter(ap -> isOwnerLikeRole(ap.getRole()))
                .count();

        List<PermissionAccountRow> allRows = new ArrayList<>();
        for (Account account : accountRepository.findAll()) {
            if (account.getId() == null || !matchesAccount(account, needle)) {
                continue;
            }

            Accountpermission ap = assignmentByAccount.get(account.getId());
            String role = ap == null ? null : canonicalDisplayRole(ap.getRole());
            boolean lastOwner = isOwnerLikeRole(role) && ownerLikeCount <= 1;
            String roleDisplay = (role == null || role.isBlank())
                    ? "Không quyền"
                    : RoleConstants.vietnameseName(role);

            allRows.add(new PermissionAccountRow(
                    account.getId(), account.getName(), usernameOrEmail(account),
                    role, roleDisplay, lastOwner));
        }

        // Owner(s) first, then by account name, then by id.
        allRows.sort(Comparator
                .comparing(PermissionAccountRow::isAssigned).reversed()
                .thenComparing(row -> normalizedName(row.getAccountName()))
                .thenComparing(row -> row.getAccountId() == null ? Integer.MAX_VALUE : row.getAccountId()));

        // In-memory pagination by account row (the account set is small).
        long totalElements = allRows.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int boundedPage = totalPages == 0 ? 0 : Math.min(safePage, totalPages - 1);
        int fromIndex = boundedPage * safeSize;
        List<PermissionAccountRow> pageRows = fromIndex >= allRows.size()
                ? List.of()
                : List.copyOf(allRows.subList(fromIndex, Math.min(fromIndex + safeSize, allRows.size())));

        return new PermissionPageView(pageRows, boundedPage, safeSize, totalElements, totalPages, search);
    }

    /**
     * Saves one account's system-wide role (a dropdown change on the Permission Table).
     *
     * <ul>
     *   <li>A blank/empty {@code role} clears the assignment: the existing
     *       {@code AccountPermission} row (if any) is deleted, meaning "Không quyền".</li>
     *   <li>Only {@code PHARMACIST}, {@code ACCOUNTANT} and {@code OWNER} can be assigned; anything
     *       else (including the legacy {@code CHIEF_PHARMACIST}) is rejected.</li>
     *   <li>If the account currently holds an owner-like role and is the last one in the system,
     *       changing it away from Owner is rejected — the system must always keep at least one
     *       Owner.</li>
     *   <li>Otherwise the role is created (with a manual id — the PK is not guaranteed
     *       auto-increment on every environment) or updated in place.</li>
     * </ul>
     */
    @Transactional
    public void saveRole(Integer accountId, String role) {
        if (accountId == null) {
            throw new IllegalArgumentException(MSG_MISSING_ACCOUNT);
        }

        Account account = requireAccount(accountId);

        String normalized = canonicalRole(role);
        boolean clearing = normalized == null || normalized.isBlank();
        if (!clearing && !RoleConstants.isPermissionTableRole(normalized)) {
            throw new IllegalArgumentException(MSG_INVALID_ROLE);
        }

        Accountpermission existing = findAssignment(accountId);
        boolean wasOwnerLike = existing != null && isOwnerLikeRole(existing.getRole());
        boolean staysOwner = !clearing && RoleConstants.OWNER.equals(normalized);

        // Never let a save take the last remaining Owner away from Owner.
        if (wasOwnerLike && !staysOwner
                && accountpermissionRepository.findOwnerLikeAssignments().size() <= 1) {
            throw new IllegalArgumentException(MSG_LAST_OWNER);
        }

        if (clearing) {
            if (existing != null) {
                accountpermissionRepository.delete(existing);
                invalidateSessions(accountId);
            }
            return;
        }

        if (existing == null) {
            Accountpermission permission = new Accountpermission();
            // Manual id assignment, matching the existing findMaxId()+1 convention for this table.
            permission.setId(accountpermissionRepository.findMaxId() + 1);
            permission.setAccountID(account);
            permission.setRole(normalized);
            accountpermissionRepository.save(permission);
        } else {
            existing.setRole(normalized);
            accountpermissionRepository.save(existing);
        }
        invalidateSessions(accountId);
    }

    private void invalidateSessions(Integer accountId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof AccountPrincipal ap && ap.getAccountId().equals(accountId))
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .forEach(SessionInformation::expireNow);
    }

    // ------------------------------------------------------------------

    /** Current assignment for an account, or {@code null} (one-role-per-account rule). */
    private Accountpermission findAssignment(Integer accountId) {
        List<Accountpermission> existing = accountpermissionRepository.findByAccountId(accountId);
        return existing.isEmpty() ? null : existing.get(0);
    }

    private Account requireAccount(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_ACCOUNT_NOT_FOUND));
    }

    /**
     * The role to display/compare for a stored value: the legacy {@code CHIEF_PHARMACIST} role is
     * shown merged into {@code OWNER} ("Chủ nhà thuốc"), matching the single-store role model.
     */
    private String canonicalDisplayRole(String rawRole) {
        String normalized = canonicalRole(rawRole);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        return RoleConstants.CHIEF_PHARMACIST.equals(normalized) ? RoleConstants.OWNER : normalized;
    }

    /** True for {@code OWNER} and the legacy {@code CHIEF_PHARMACIST} (merged owner identity). */
    private boolean isOwnerLikeRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = canonicalRole(role);
        return RoleConstants.OWNER.equals(normalized) || RoleConstants.CHIEF_PHARMACIST.equals(normalized);
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
