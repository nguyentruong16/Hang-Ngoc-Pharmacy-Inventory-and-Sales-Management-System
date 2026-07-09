package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Form backing the "Create customer return" screen. The user picks one completed sale invoice, then
 * chooses how many units of each of its lines to return, a refund method and a reason.
 *
 * <p>The refund amount is <strong>not</strong> taken from the client — the service computes it from
 * the original invoice-line sell prices. {@code returnType} only decides which refund bucket the
 * total lands in (cash / bank transfer / credited against debt).</p>
 */
@Getter
@Setter
public class ReturnCreateRequest {

    /** The original sale invoice being returned against. */
    private Integer invoiceId;

    /** Refund method: {@code CASH} / {@code BANKING} / {@code DEBT}. */
    private String returnType;

    /** Reason for the return (required). */
    private String reason;

    /** Optional free-text note. */
    private String note;

    /** One entry per candidate invoice line; blank/zero rows are dropped server-side. */
    private List<ReturnLineRequest> items = new ArrayList<>();
}
