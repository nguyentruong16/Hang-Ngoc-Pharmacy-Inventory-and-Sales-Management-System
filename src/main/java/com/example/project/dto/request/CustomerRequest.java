package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Vui lòng chọn loại khách hàng")
    @Pattern(regexp = "INDIVIDUAL|COMPANY", message = "Loại khách hàng không hợp lệ")
    private String customerType = "INDIVIDUAL";

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0(2|3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 02, 03, 05, 07, 08 hoặc 09"
    )
    private String phoneNumber;

    @Size(max = 100, message = "Mã số thuế không được vượt quá 100 ký tự")
    private String taxCode;

    @Size(max = 100, message = "Địa chỉ không được vượt quá 100 ký tự")
    private String address;

    @Size(max = 100, message = "Số tài khoản không được vượt quá 100 ký tự")
    private String bankAccountNumber;

    @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự")
    private String bankName;

    private String note;
}
