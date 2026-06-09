package com.example.project.dto.response;

import com.example.project.entity.Origin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OriginResponse {
    private Integer id;
    private String name;

    public static OriginResponse from(Origin origin) {
        return new OriginResponse(
                origin.getId(),
                origin.getName()
        );
    }
}