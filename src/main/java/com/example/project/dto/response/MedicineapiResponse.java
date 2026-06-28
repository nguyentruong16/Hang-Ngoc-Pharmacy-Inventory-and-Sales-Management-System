package com.example.project.dto.response;

import com.example.project.entity.Medicineapi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineapiResponse {
    private Integer id;
    private Integer productId;
    private String apiName;
    private String strength;

    public static MedicineapiResponse from(Medicineapi medicineapi) {
        return new MedicineapiResponse(
                medicineapi.getId(),
                medicineapi.getProductID() != null ? medicineapi.getProductID().getProductID() : null,
                medicineapi.getApiName(),
                medicineapi.getStrength()
        );
    }
}