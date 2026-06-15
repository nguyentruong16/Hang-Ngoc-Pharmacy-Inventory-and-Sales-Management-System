package com.example.project.dto.response;

import com.example.project.entity.Branch;
import com.example.project.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Integer id;
    private String name;
    private String address;
    private String statusName;

    public static BranchResponse from(Branch branch) {
        Status status = branch.getStatusID();

        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                status != null ? status.getName() : null
        );
    }
}
