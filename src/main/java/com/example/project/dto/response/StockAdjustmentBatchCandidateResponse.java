package com.example.project.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StockAdjustmentBatchCandidateResponse {

    private Integer batchId;

    private Integer productId;
    private String productName;

    private String lotNumber;

    // Inlined into JS via Thymeleaf, whose serializer has no Java-8-time module. The client only uses
    // the preformatted display string, so keep the raw LocalDate out of JSON serialization.
    @JsonIgnore
    private LocalDate expirationDate;
    private String expirationDateDisplay;

    private Integer storageQuantity;

    private Integer productUnitId;
    private String unitName;

    private BigDecimal unitCostPrice;

    // Giá bán (đã gồm VAT) của đơn vị cơ sở + thuế suất thường của sản phẩm — để màn tạo prefill
    // ô "Thuế suất GTGT đầu ra" khi loại phiếu là INTERNAL_USE/GIFT/SAMPLE (tính thuế theo GIÁ BÁN).
    private BigDecimal sellPrice;
    private BigDecimal defaultVatRate;
}