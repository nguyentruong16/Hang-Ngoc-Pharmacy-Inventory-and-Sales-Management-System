package com.example.project.dto.response;

import com.example.project.entity.Combocomponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CombocomponentResponse {
    private Integer id;
    private String comboId;
    private String componentProductId;
    private Integer componentUnitId;
    private BigDecimal quantity;
    private String note;

    public static CombocomponentResponse from(Combocomponent combocomponent) {
        return new CombocomponentResponse(
                combocomponent.getId(),
                combocomponent.getComboID() != null ? combocomponent.getComboID().getComboID() : null,
                combocomponent.getComponentProductID() != null ? combocomponent.getComponentProductID().getProductID() : null,
                combocomponent.getComponentUnitID() != null ? combocomponent.getComponentUnitID().getId() : null,
                combocomponent.getQuantity(),
                combocomponent.getNote()
        );
    }
}