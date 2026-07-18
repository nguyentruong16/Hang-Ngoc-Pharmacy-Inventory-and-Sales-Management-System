package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Minimal (id, name) projection of a {@link com.example.project.entity.Supplier}, used for the
 * supplier search field on the Purchase Invoice create page — same reasoning as
 * {@link ProductOptionResponse}: this list is embedded into inline JavaScript via
 * {@code th:inline="javascript"}, so only the two fields the picker actually needs are carried.
 */
@Getter
@AllArgsConstructor
public class SupplierOptionResponse {

    private Integer id;

    private String name;
}
