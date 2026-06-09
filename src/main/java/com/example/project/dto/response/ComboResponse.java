package com.example.project.dto.response;

import com.example.project.entity.Combo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComboResponse {
    private String comboID;
    private String productId;

    public static ComboResponse from(Combo combo) {
        return new ComboResponse(
                combo.getComboID(),
                combo.getProduct() != null ? combo.getProduct().getProductID() : null
        );
    }
}