package com.example.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OwnerUserCreateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09"
    )
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải có từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String role;

    @NotEmpty(message = "Vui lòng chọn ít nhất một chi nhánh")
    private List<Integer> branchIds = new ArrayList<>();

    private Boolean status = true;
}