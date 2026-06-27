package com.example.project.context;

import com.example.project.constant.RoleConstants;
import com.example.project.security.AccountPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Single, isolated point that reads "who is the current user" from Spring Security.
 *
 * <p>The source of truth is the {@link AccountPrincipal} placed in the
 * {@link SecurityContextHolder} at login by {@code CustomAccountDetailsService}. There is no
 * session-based or default-role fallback: if there is no authenticated {@link AccountPrincipal}
 * every accessor returns {@code null}, and callers must treat that as "not signed in" rather
 * than assuming any role.</p>
 */
@Component
public class CurrentUserContext {

    /** The authenticated principal, or {@code null} for anonymous/unauthenticated requests. */
    public AccountPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof AccountPrincipal accountPrincipal ? accountPrincipal : null;
    }

    public boolean isAuthenticated() {
        return getPrincipal() != null;
    }

    /** Bare primary role (e.g. {@code "OWNER"}) of the signed-in user, or {@code null}. */
    public String getCurrentRole() {
        AccountPrincipal principal = getPrincipal();
        return principal == null ? null : principal.getPrimaryRole();
    }

    public String getCurrentAccountName() {
        AccountPrincipal principal = getPrincipal();
        return principal == null ? null : principal.getDisplayName();
    }

    public Integer getCurrentAccountId() {
        AccountPrincipal principal = getPrincipal();
        return principal == null ? null : principal.getAccountId();
    }

    public Integer getCurrentBranchId() {
        AccountPrincipal principal = getPrincipal();
        return principal == null ? null : principal.getBranchId();
    }

    /** Whether the signed-in user is the Owner (cross-branch access, F-24). */
    public boolean isOwner() {
        return RoleConstants.OWNER.equals(getCurrentRole());
    }

    /**
     * Branch to scope queries to, or {@code null} = no restriction (all branches).
     *
     * <p><strong>Branch isolation convention:</strong> branch-scoped queries MUST read the
     * active branch through this accessor, never via {@link #getCurrentBranchId()} directly. The
     * Owner is cross-branch, so this returns {@code null} for the Owner (and for anonymous), which
     * callers must treat as "do not filter by branch". A typical repository query is then
     * {@code where (:branchId is null or e.branchID.id = :branchId)}.</p>
     */
    public Integer getBranchFilter() {
        if (isOwner()) {
            return null;
        }
        return getCurrentBranchId();
    }
}
