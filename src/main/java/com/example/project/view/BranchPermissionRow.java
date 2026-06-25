package com.example.project.view;

import lombok.Getter;

/**
 * One row of the per-branch user table: a single account together with the role it holds at the
 * currently selected branch. Built inside a transaction so the Thymeleaf view never touches lazy
 * associations.
 *
 * <p>A {@code null} {@link #role} means "not yet assigned" (rendered as "Chưa phân quyền").
 * {@link #ownerLocked} is {@code true} for the system Owner, whose role can never be edited.
 * {@link #editable} is {@code true} only when the branch is active <em>and</em> the row is not the
 * Owner — i.e. exactly when the view should show a role dropdown instead of a read-only badge.</p>
 */
@Getter
public class BranchPermissionRow {

    private final Integer accountId;
    private final String accountName;
    private final String usernameOrEmail;
    private final String role;          // raw role code, e.g. "CASHIER"; null when unassigned
    private final String roleDisplay;   // Vietnamese label, or "Chưa phân quyền" when unassigned
    private final boolean ownerLocked;  // true for the Owner row (read-only everywhere)
    private final boolean editable;     // branch active AND not the Owner row

    public BranchPermissionRow(Integer accountId, String accountName, String usernameOrEmail,
                               String role, String roleDisplay,
                               boolean ownerLocked, boolean editable) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.usernameOrEmail = usernameOrEmail;
        this.role = role;
        this.roleDisplay = roleDisplay;
        this.ownerLocked = ownerLocked;
        this.editable = editable;
    }

    /** True when this account currently holds a role at the selected branch. */
    public boolean isAssigned() {
        return role != null && !role.isBlank();
    }
}
