package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** On-hand stock of a product at one branch, for the "Tồn kho theo chi nhánh" block. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBranchStockResponse {
    private Integer branchId;
    private String branchName;
    private long totalStock;
    private String stockStatusLabel;
    private String stockStatusCss;
}
