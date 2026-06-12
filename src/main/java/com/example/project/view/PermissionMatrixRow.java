package com.example.project.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * One row of the (read-only) Permission Table: a module plus, for every action column,
 * whether the currently-selected role is allowed to perform it.
 *
 * <p>Backed by code/config only — see {@code PermissionMatrixService}. There is no
 * Permission/RolePermission table, so this is never persisted and the table is display-only.</p>
 */
@Getter
@AllArgsConstructor
public class PermissionMatrixRow {

    private final String module;

    /** action label -> allowed. Iteration order follows the canonical action list. */
    private final Map<String, Boolean> actions;
}
