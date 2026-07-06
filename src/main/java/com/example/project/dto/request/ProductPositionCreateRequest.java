package com.example.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One storage-location row of the Create Product form (persisted as a Position). */
@Getter
@Setter
@NoArgsConstructor
public class ProductPositionCreateRequest {
    private String name;
}
