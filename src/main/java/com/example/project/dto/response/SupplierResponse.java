package com.example.project.dto.response;

import com.example.project.entity.Supplier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierResponse {
    private Integer id;
    private String supplierCode;
    private String name;
    private String address;
    private String phone;
    private String email;
    private long productCount;

    public static SupplierResponse from(Supplier supplier) {
        SupplierResponse r = new SupplierResponse();
        r.id = supplier.getId();
        r.supplierCode = formatCode(supplier.getId());
        r.name = supplier.getName();
        r.address = supplier.getAddress();
        r.phone = supplier.getPhone();
        r.email = supplier.getEmail();
        return r;
    }

    /** Mã nhà cung cấp suy ra trực tiếp từ khóa chính auto-increment: {@code NCC-[supplierID]}. */
    private static String formatCode(Integer id) {
        if (id == null) return "NCC";
        return "NCC-" + id;
    }
}
