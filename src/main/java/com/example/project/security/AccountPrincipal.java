package com.example.project.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Authenticated user backing the Spring Security context.
 *
 * <p>In addition to the standard {@link UserDetails} contract it exposes the navigation
 * data the UI needs: {@link #getPrimaryRole() primaryRole} (a bare role such as
 * {@code "OWNER"} from {@code Accountpermission.role}) and {@link #getBranchId() branchId}
 * (from {@code Accountpermission.branchID}). These come from the user's primary
 * AccountPermission row, chosen in {@code CustomAccountDetailsService}.</p>
 */
public class AccountPrincipal implements UserDetails {
    private final Integer accountId;
    private final String displayName;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String primaryRole;
    private final Integer branchId;

    public AccountPrincipal(
            Integer accountId,
            String displayName,
            String username,
            String email,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities,
            String primaryRole,
            Integer branchId) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
        this.primaryRole = primaryRole;
        this.branchId = branchId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    /** Bare role name (e.g. {@code "OWNER"}) of the user's primary AccountPermission row. */
    public String getPrimaryRole() {
        return primaryRole;
    }

    /** Branch of the user's primary AccountPermission row, or {@code null} if unset. */
    public Integer getBranchId() {
        return branchId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AccountPrincipal that)) {
            return false;
        }
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
