package com.example.project.dto.response;

import com.example.project.entity.Purchaseinvoice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseinvoiceResponse {
    private Integer id;
    private String purchaseInvoiceCode;
    private Instant date;
    private Integer supplierId;
    private Integer employeeId;
    private BigDecimal additionCost;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private Integer procurementID;
    private BigDecimal paid;
    private String status;
    private String note;

    public static PurchaseinvoiceResponse from(Purchaseinvoice purchaseinvoice) {
        return new PurchaseinvoiceResponse(
                purchaseinvoice.getId(),
                purchaseinvoice.getPurchaseInvoiceCode(),
                purchaseinvoice.getDate(),
                purchaseinvoice.getSupplierID() != null ? purchaseinvoice.getSupplierID().getId() : null,
                purchaseinvoice.getEmployeeID() != null ? purchaseinvoice.getEmployeeID().getId() : null,
                purchaseinvoice.getAdditionCost(),
                purchaseinvoice.getDiscount(),
                purchaseinvoice.getTotalAmount(),
                purchaseinvoice.getProcurementID() != null ? purchaseinvoice.getProcurementID().getId() : null,
                purchaseinvoice.getPaid(),
                purchaseinvoice.getStatus(),
                purchaseinvoice.getNote()
        );
    }
}