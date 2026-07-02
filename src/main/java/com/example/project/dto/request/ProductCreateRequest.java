package com.example.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Payload of the Create Product form.
 *
 * <p>{@code itemGroup} (Thuốc / Hàng hóa / Thiết bị / TPCN) only drives which sections the form
 * shows; it is NOT persisted as a new column — the persisted classification is {@code Type}
 * ({@code typeId}). This form creates only the product master: Product + ProductUnit + optional
 * MedicineAPI + optional Position. It does not touch Batch/stock or expiry — expiry lives on
 * individual batches created later during goods-receipt.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequest {

    /** Form-only selector (MEDICINE / GOODS / DEVICE / SUPPLEMENT); not persisted. */
    private String itemGroup;

    private String name;
    private String code;
    private String barcode;
    private Integer typeId;
    private Integer producerId;
    private Integer originId;
    private String registrationNumber;
    private Integer minStock;
    private Integer maxStock;
    private Boolean status;
    private String note;

    private List<ProductUnitCreateRequest> units = new ArrayList<>();
    private List<ProductIngredientCreateRequest> ingredients = new ArrayList<>();
    private List<ProductPositionCreateRequest> positions = new ArrayList<>();
}
