package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * One posted line of the "Create supplier return" form: how many base units of a given purchase
 * line to return. Blank/zero rows are dropped server-side.
 */
@Getter
@Setter
public class ReturnPurchaseLineRequest {

    /** The original purchase-invoice detail line being returned. */
    private Integer purchaseDetailId;

    /** Quantity to return, in the batch's base (storage) unit. */
    private Integer returnQty;
}
