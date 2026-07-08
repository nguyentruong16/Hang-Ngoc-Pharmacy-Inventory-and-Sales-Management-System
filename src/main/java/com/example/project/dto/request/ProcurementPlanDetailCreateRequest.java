package com.example.project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProcurementPlanDetailCreateRequest {

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Integer productId;

    @NotNull(message = "Số lượng dự trù không được để trống")
    @Min(value = 1, message = "Số lượng dự trù phải lớn hơn 0")
    private Integer requestedQuantity;

    @Size(max = 20, message = "Đơn vị không được vượt quá 20 ký tự")
    private String unit;

    @DecimalMin(value = "0.0", message = "Giá dự kiến không được âm")
    private BigDecimal estimatedPrice;

    private Integer supplierId;
}
