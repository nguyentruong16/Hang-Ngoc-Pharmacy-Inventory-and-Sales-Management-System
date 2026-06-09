package com.example.project.dto.response;

import com.example.project.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private Integer id;
    private Integer branchId;
    private String name;

    public static PositionResponse from(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getBranchID() != null ? position.getBranchID().getId() : null,
                position.getName()
        );
    }
}