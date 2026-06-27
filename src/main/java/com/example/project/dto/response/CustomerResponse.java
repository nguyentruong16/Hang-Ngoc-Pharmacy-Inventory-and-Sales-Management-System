package com.example.project.dto.response;

import com.example.project.entity.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerResponse {
    private Integer id;
    private String customerCode;
    private String customerType;
    private String customerTypeLabel;
    private String name;
    private String taxCode;
    private String address;
    private String bankAccountNumber;
    private String bankName;
    private String phoneNumber;
    private String note;

    public static CustomerResponse from(Customer customer) {
        CustomerResponse r = new CustomerResponse();
        r.id = customer.getId();
        r.customerCode = formatCode(customer.getId());
        r.customerType = customer.getCustomerType();
        r.customerTypeLabel = typeLabel(customer.getCustomerType());
        r.name = customer.getName();
        r.taxCode = customer.getTaxCode();
        r.address = customer.getAddress();
        r.bankAccountNumber = customer.getBankAccountNumber();
        r.bankName = customer.getBankName();
        r.phoneNumber = customer.getPhoneNumber();
        r.note = customer.getNote();
        return r;
    }

    public boolean isCompany() {
        return "COMPANY".equals(customerType);
    }

    /** Mã khách hàng suy ra từ khóa chính auto-increment, zero-pad 6 chữ số: {@code CUS-000006}. */
    private static String formatCode(Integer id) {
        if (id == null) return "CUS-000000";
        return "CUS-" + String.format("%06d", id);
    }

    /** Nhãn tiếng Việt cho loại khách hàng. */
    public static String typeLabel(String type) {
        if ("COMPANY".equals(type)) return "Doanh nghiệp";
        if ("INDIVIDUAL".equals(type)) return "Cá nhân";
        return "—";
    }
}