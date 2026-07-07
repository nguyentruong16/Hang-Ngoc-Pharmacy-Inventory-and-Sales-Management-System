package com.example.project.view;

import lombok.Getter;

/**
 * One row of the flat, single-store Permission Table: an account together with the single role it
 * holds system-wide. Built inside a transaction so the Thymeleaf view never touches lazy
 * associations.
 *
 * <p>A {@code null}/blank {@link #role} means "Không quyền" (no {@code Accountpermission} row).</p>
 *
 * <p>{@link #lastOwner} is {@code true} only when this account currently holds an owner-like role
 * <em>and</em> is the only one left in the system; the screen renders such a row read-only so it
 * can never be demoted down to zero owners.</p>
 */
@Getter
public class PermissionAccountRow {

    private final Integer accountId;
    private final String accountName;
    private final String usernameOrEmail;
    private final String role;          // raw role code, e.g. "PHARMACIST"/"OWNER"; null when unassigned
    private final String roleDisplay;   // Vietnamese label, or "Không quyền" when unassigned
    private final boolean lastOwner;    // the only remaining owner-like account — protected from demotion

    public PermissionAccountRow(Integer accountId, String accountName, String usernameOrEmail,
                                String role, String roleDisplay, boolean lastOwner) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.usernameOrEmail = usernameOrEmail;
        this.role = role;
        this.roleDisplay = roleDisplay;
        this.lastOwner = lastOwner;
    }

    /** True when this account currently holds a role. */
    public boolean isAssigned() {
        return role != null && !role.isBlank();
    }
}
