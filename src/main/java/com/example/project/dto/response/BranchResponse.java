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
    private Integer id;
    private String name;
    private String address;
    private Integer statusId;

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getStatusID() != null ? branch.getStatusID().getId() : null
        );
    }
}