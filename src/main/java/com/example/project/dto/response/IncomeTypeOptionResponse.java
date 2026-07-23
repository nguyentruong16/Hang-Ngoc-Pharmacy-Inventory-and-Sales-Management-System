package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** One income-type option for dropdowns (code + Vietnamese label). */
@Getter
@AllArgsConstructor
public class IncomeTypeOptionResponse {

    public static final String SUPPLIER = "SUPPLIER";
    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String OTHER = "OTHER";

    private String code;
    private String label;

    public static List<IncomeTypeOptionResponse> all() {
        return List.of(
                new IncomeTypeOptionResponse(SUPPLIER, "Nhà cung cấp"),
                new IncomeTypeOptionResponse(EMPLOYEE, "Nhân viên"),
                new IncomeTypeOptionResponse(CUSTOMER, "Khách hàng"),
                new IncomeTypeOptionResponse(OTHER, "Khác")
        );
    }

    public static boolean isValid(String type) {
        return type != null && all().stream().anyMatch(option -> option.code.equals(type));
    }

    public static String labelOf(String type) {
        if (type == null) {
            return "";
        }
        return all().stream()
                .filter(option -> option.code.equals(type))
                .map(IncomeTypeOptionResponse::getLabel)
                .findFirst()
                .orElse(type);
    }
}
