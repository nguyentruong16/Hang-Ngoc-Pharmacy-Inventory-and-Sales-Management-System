package com.example.project.view;

import lombok.Getter;

/**
 * One branch button/tab on the Owner permission screen. Carries everything the view needs to
 * render the button (name, active vs inactive styling, the selected state) without touching the
 * lazy {@code Branch -> Status} association at render time.
 *
 * <p>A branch is {@code active} only when its status name is exactly {@code "Đang hoạt động"};
 * anything else (including a {@code null} status) is treated as inactive and shown as
 * {@code "Đã ngừng hoạt động"}.</p>
 */
@Getter
public class PermissionBranchOption {

    private final Integer id;
    private final String name;
    private final String statusName;     // raw status name from the DB, may be null
    private final boolean active;
    private final boolean selected;
    private final String displayStatus;  // "Đang hoạt động" / "Đã ngừng hoạt động"

    public PermissionBranchOption(Integer id, String name, String statusName,
                                  boolean active, boolean selected, String displayStatus) {
        this.id = id;
        this.name = name;
        this.statusName = statusName;
        this.active = active;
        this.selected = selected;
        this.displayStatus = displayStatus;
    }
}
