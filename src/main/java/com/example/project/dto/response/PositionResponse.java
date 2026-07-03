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
    private Integer productId;
    private String productName;
    private String productCode;
    private Integer branchId;
    private String branchName;
    private String name;

    public static PositionResponse from(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getProductID() != null ? position.getProductID().getProductID() : null,
                position.getProductID() != null ? position.getProductID().getName() : null,
                position.getProductID() != null ? position.getProductID().getCode() : null,
                position.getBranchID() != null ? position.getBranchID().getId() : null,
                position.getBranchID() != null ? position.getBranchID().getName() : null,
                position.getName()
        );
    }
}
