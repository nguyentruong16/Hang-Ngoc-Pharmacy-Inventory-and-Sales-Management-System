package com.example.project.view;

import lombok.Getter;

import java.util.Map;

/**
 * One row of the Owner permission matrix: a single account together with its role at every
 * branch. Cells are keyed by branch id; {@link #getCell(Integer)} never returns {@code null}
 * so the view can always render a dropdown, even for branches the account has no assignment at.
 */
@Getter
public class PermissionMatrixRow {

    private final Integer accountId;
    private final String accountName;
    private final String usernameOrEmail;
    private final boolean ownerAccount;
    private final Map<Integer, PermissionMatrixCell> cells;

    public PermissionMatrixRow(Integer accountId, String accountName, String usernameOrEmail,
                               boolean ownerAccount,
                               Map<Integer, PermissionMatrixCell> cells) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.usernameOrEmail = usernameOrEmail;
        this.ownerAccount = ownerAccount;
        this.cells = cells;
    }

    public PermissionMatrixCell getCell(Integer branchId) {
        PermissionMatrixCell cell = cells.get(branchId);
        return cell != null ? cell : PermissionMatrixCell.empty(accountId, branchId);
    }
}
