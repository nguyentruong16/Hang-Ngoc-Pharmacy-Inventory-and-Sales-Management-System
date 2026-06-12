package com.example.project.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class AccountPrincipal implements UserDetails {
    private final Integer accountId;
    private final String displayName;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public AccountPrincipal(
            Integer accountId,
            String displayName,
            String username,
            String email,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
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
