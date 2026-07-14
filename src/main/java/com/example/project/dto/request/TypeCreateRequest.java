package com.example.project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TypeCreateRequest {

    @NotBlank(message = "Nhóm mặt hàng không được để trống")
    @Size(max = 100, message = "Nhóm mặt hàng không được vượt quá 100 ký tự")
    private String sortType;

    @NotBlank(message = "Tên loại hàng không được để trống")
    @Size(max = 100, message = "Tên loại hàng không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Tỷ lệ VAT mặc định không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ VAT mặc định không được âm")
    private BigDecimal defaultVATRate;
}
