package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Summary cards shown above the Product List. All counts are derived from the real
 * product set and their current stock (SUM of Batch.storageQuantity vs minStock).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListStatsResponse {
    /** Total number of products. */
    private long totalProducts;
    /** Products with stock above the minimum threshold. */
    private long inStockCount;
    /** Products with stock at/below the minimum threshold but still > 0. */
    private long lowStockCount;
    /** Products with no stock (stock <= 0). */
    private long outOfStockCount;
}
