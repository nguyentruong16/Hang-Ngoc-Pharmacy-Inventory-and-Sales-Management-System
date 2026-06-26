package com.example.project.dto.response;

import com.example.project.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Integer id;
    private String customerType;
    private String name;
    private String taxCode;
    private String address;
    private String bankAccountNumber;
    private String bankName;
    private String phoneNumber;
    private String note;

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerType(),
                customer.getName(),
                customer.getTaxCode(),
                customer.getAddress(),
                customer.getBankAccountNumber(),
                customer.getBankName(),
                customer.getPhoneNumber(),
                customer.getNote()
        );
    }
}