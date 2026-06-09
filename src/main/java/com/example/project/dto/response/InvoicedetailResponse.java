package com.example.project.dto.response;

import com.example.project.entity.Invoicedetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoicedetailResponse {
    private Integer id;
    private Integer invoiceId;
    private String productId;
    private Integer productUnitId;
    private Integer batchId;
    private Integer quantity;
    private String unitName;
    private Integer baseQtyDeducted;
    private BigDecimal unitSellPrice;
    private BigDecimal unitImportPrice;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal subimport;
    private BigDecimal subtotal;
    private BigDecimal lineTotal;
    private Integer returnedQty;

    public static InvoicedetailResponse from(Invoicedetail invoicedetail) {
        return new InvoicedetailResponse(
                invoicedetail.getId(),
                invoicedetail.getInvoiceID() != null ? invoicedetail.getInvoiceID().getId() : null,
                invoicedetail.getProductID() != null ? invoicedetail.getProductID().getProductID() : null,
                invoicedetail.getProductUnitID() != null ? invoicedetail.getProductUnitID().getId() : null,
                invoicedetail.getBatchID() != null ? invoicedetail.getBatchID().getId() : null,
                invoicedetail.getQuantity(),
                invoicedetail.getUnitName(),
                invoicedetail.getBaseQtyDeducted(),
                invoicedetail.getUnitSellPrice(),
                invoicedetail.getUnitImportPrice(),
                invoicedetail.getVatRate(),
                invoicedetail.getVatAmount(),
                invoicedetail.getSubimport(),
                invoicedetail.getSubtotal(),
                invoicedetail.getLineTotal(),
                invoicedetail.getReturnedQty()
        );
    }
}