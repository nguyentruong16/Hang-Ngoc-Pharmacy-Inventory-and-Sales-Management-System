package com.example.project.dto.response;

import com.example.project.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypeResponse {
    private Integer id;
    private String sortType;
    private String name;
    private BigDecimal defaultVATRate;

    public static TypeResponse from(Type type) {
        return new TypeResponse(
                type.getId(),
                type.getSortType(),
                type.getName(),
                type.getDefaultVATRate()
        );
    }
}