package com.example.project.dto.response;

import com.example.project.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypeResponse {
    private Integer id;
    private Integer sortType;
    private String name;

    public static TypeResponse from(Type type) {
        return new TypeResponse(
                type.getId(),
                type.getSortType(),
                type.getName()
        );
    }
}