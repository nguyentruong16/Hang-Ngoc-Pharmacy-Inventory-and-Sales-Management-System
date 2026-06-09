package com.example.project.dto.response;

import com.example.project.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private Integer id;
    private String name;

    public static StatusResponse from(Status status) {
        return new StatusResponse(
                status.getId(),
                status.getName()
        );
    }
}