package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * One prospective return line posted from the create screen: how many units of a specific original
 * invoice line the customer is bringing back, and whether that quantity can go back on the shelf.
 *
 * <p>Quantities are never trusted blindly — the service re-derives the sold/already-returned figures
 * from the original {@code Invoicedetail} and rejects anything above the still-returnable amount.</p>
 */
@Getter
@Setter
public class ReturnLineRequest {

    /** The original {@code Invoicedetail} this line returns against. */
    private Integer invoiceDetailId;

    /** Units returned, in the sale unit. Rows with a null/zero quantity are ignored. */
    private Integer returnQty;

    /** Whether the returned units can be restocked (false = damaged/expired, do not add back to the batch). */
    private Boolean restockable;
}
