package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Form backing the "Create supplier return" screen. The Owner picks one received purchase invoice,
 * then chooses how many units of each of its lines to return to the supplier, a refund method and a
 * reason.
 *
 * <p>The refund amount is <strong>not</strong> taken from the client — the service computes it from
 * each batch's import cost. {@code returnType} only decides which refund bucket the total lands in
 * (cash / bank transfer / credited against the payable to the supplier).</p>
 */
@Getter
@Setter
public class ReturnPurchaseCreateRequest {

    /** The original purchase invoice being returned against. */
    private Integer purchaseId;

    /** Refund method: {@code CASH} / {@code BANKING} / {@code DEBT}. */
    private String returnType;

    /** Reason for the return (required). */
    private String reason;

    /** Optional free-text note. */
    private String note;

    /** One entry per candidate purchase line; blank/zero rows are dropped server-side. */
    private List<ReturnPurchaseLineRequest> items = new ArrayList<>();
}
