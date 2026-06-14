package com.example.project.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * One row of the Owner "Phân quyền theo chi nhánh" (role-by-branch) table: a single
 * AccountPermission flattened with its account and branch display data. Built inside a
 * transaction so the Thymeleaf view never touches lazy associations.
 */
@Getter
@AllArgsConstructor
public class PermissionAssignmentRow {

    private final Integer id;          // accountPermissionID
    private final Integer accountId;
    private final String accountName;
    private final String username;
    private final String email;
    private final Integer branchId;
    private final String branchName;
    private final String role;         // raw role code, e.g. "CASHIER"
    private final String roleDisplay;  // Vietnamese label, e.g. "Thu ngân"
}
