package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeCreateRequest {

    @NotBlank(message = "Nhóm mặt hàng không được để trống")
    @Size(max = 100, message = "Nhóm mặt hàng không được vượt quá 100 ký tự")
    private String sortType;

    @NotBlank(message = "Tên loại hàng không được để trống")
    @Size(max = 100, message = "Tên loại hàng không được vượt quá 100 ký tự")
    private String name;
}
