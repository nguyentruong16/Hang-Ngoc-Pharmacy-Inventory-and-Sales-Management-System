package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OriginCreateRequest {

    @NotBlank(message = "Tên xuất xứ không được để trống")
    @Size(max = 100, message = "Tên xuất xứ không được vượt quá 100 ký tự")
    private String name;
}
