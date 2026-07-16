package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** (id, name, phone) of a customer, for the create-invoice customer picker. */
@Getter
@AllArgsConstructor
public class CustomerOptionResponse {

    private Integer id;
    private String name;
    private String phoneNumber;
}
