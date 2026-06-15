package com.example.project.dto.response;

import com.example.project.entity.Branch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private static final String ACTIVE_STATUS_NAME = "Đang hoạt động";
    private static final String INACTIVE_STATUS_NAME = "Ngừng hoạt động";

    private Integer id;
    private String name;
    private String address;
    private Integer statusId;
    private String statusName;
    private boolean active;

    public static BranchResponse from(Branch branch) {
        String rawStatusName = branch.getStatusID() != null ? branch.getStatusID().getName() : null;
        boolean active = ACTIVE_STATUS_NAME.equalsIgnoreCase(rawStatusName);
        String statusName = rawStatusName != null ? rawStatusName : INACTIVE_STATUS_NAME;

        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getStatusID() != null ? branch.getStatusID().getId() : null,
                statusName,
                active
        );
    }
}
