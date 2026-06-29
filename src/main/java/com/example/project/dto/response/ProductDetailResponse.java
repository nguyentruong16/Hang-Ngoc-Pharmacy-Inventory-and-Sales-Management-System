package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Full payload for the Product Detail screen. All values are display-ready and already
 * branch-scoped by {@link com.example.project.service.ProductService} according to the current
 * user's role (Owner = all branches; Chief Pharmacist / Pharmacist = active branch only).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    // --- 1. General information ---
    private Integer productId;
    private String code;
    private String name;
    private String barcode;
    private String typeName;
    private String producerName;
    private String originName;
    private String registrationNumber;
    private boolean statusActive;
    private String statusLabel;
    private Integer minStock;
    private Integer maxStock;
    private String note;
    /** Active ingredients formatted as "name strength" (may be empty). */
    private List<String> ingredients;
    private List<ProductUnitDetailResponse> units;

    /** Total on-hand stock across the branches in scope. */
    private long totalStock;
    private String stockStatusLabel;
    private String stockStatusCss;

    // --- 2. Stock by branch ---
    private List<ProductBranchStockResponse> branchStocks;

    // --- 3. In-stock batches ---
    private List<ProductBatchDetailResponse> batches;

    // --- 4. Recent stock-movement preview (only populated when the role may view it) ---
    private boolean canViewRecentHistory;
    private List<ProductRecentHistoryResponse> recentHistory;
}
