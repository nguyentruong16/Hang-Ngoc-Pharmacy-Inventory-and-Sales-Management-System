package com.example.project.view;

import lombok.Getter;

/**
 * One cell of the Owner permission matrix: the role a single account holds at a single branch.
 * Built inside a transaction so the Thymeleaf view never touches lazy associations.
 *
 * <p>A cell with a {@code null} {@link #role} represents "not yet assigned" (the empty
 * "-- Chưa phân quyền --" dropdown option). {@link #ownerLocked} is {@code true} when the
 * current role is {@code OWNER}, in which case the view shows a read-only badge instead of a
 * dropdown.</p>
 */
@Getter
public class PermissionMatrixCell {

    private final Integer accountPermissionId; // existing AccountPermission id, null when unassigned
    private final Integer accountId;
    private final Integer branchId;
    private final String role;          // raw role code, e.g. "CASHIER"; null when unassigned
    private final String roleDisplay;   // Vietnamese label; "" when unassigned
    private final boolean ownerLocked;  // true when the current role is OWNER (read-only)

    public PermissionMatrixCell(Integer accountPermissionId, Integer accountId, Integer branchId,
                                String role, String roleDisplay, boolean ownerLocked) {
        this.accountPermissionId = accountPermissionId;
        this.accountId = accountId;
        this.branchId = branchId;
        this.role = role;
        this.roleDisplay = roleDisplay;
        this.ownerLocked = ownerLocked;
    }

    /** An "unassigned" cell for an account/branch pair that has no AccountPermission row. */
    public static PermissionMatrixCell empty(Integer accountId, Integer branchId) {
        return new PermissionMatrixCell(null, accountId, branchId, null, "", false);
    }
}
