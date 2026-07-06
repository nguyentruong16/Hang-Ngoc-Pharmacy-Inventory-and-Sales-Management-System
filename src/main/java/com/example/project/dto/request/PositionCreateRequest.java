package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionCreateRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private Integer productId;

    @NotBlank(message = "Tên vị trí không được để trống")
    @Size(max = 100, message = "Tên vị trí không được vượt quá 100 ký tự")
    private String name;
}
