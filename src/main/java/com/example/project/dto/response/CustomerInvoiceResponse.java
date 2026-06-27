package com.example.project.dto.response;

import com.example.project.entity.Invoice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Dòng lịch sử mua hàng hiển thị trên màn hình chi tiết khách hàng.
 * Chỉ phục vụ đọc — suy ra từ {@link Invoice} có FK tới khách hàng.
 */
@Getter
@Setter
@NoArgsConstructor
public class CustomerInvoiceResponse {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private Integer id;
    private String invoiceCode;
    private String date;
    private BigDecimal total;

    public static CustomerInvoiceResponse from(Invoice invoice) {
        CustomerInvoiceResponse r = new CustomerInvoiceResponse();
        r.id = invoice.getId();
        r.invoiceCode = "HD-" + String.format("%06d", invoice.getId());
        r.date = invoice.getDate() == null ? "—" : DATE_FMT.format(invoice.getDate());
        r.total = invoice.getTotal();
        return r;
    }
}
