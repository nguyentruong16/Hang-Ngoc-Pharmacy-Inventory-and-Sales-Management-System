package com.example.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One active-ingredient row of the Create Product form (persisted as a MedicineAPI). */
@Getter
@Setter
@NoArgsConstructor
public class ProductIngredientCreateRequest {
    private String apiName;
    private String strength;
}
