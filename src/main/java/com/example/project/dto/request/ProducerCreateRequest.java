package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProducerCreateRequest {

    @NotBlank(message = "Tên nhà sản xuất không được để trống")
    @Size(max = 255, message = "Tên nhà sản xuất không được vượt quá 255 ký tự")
    private String name;
}
