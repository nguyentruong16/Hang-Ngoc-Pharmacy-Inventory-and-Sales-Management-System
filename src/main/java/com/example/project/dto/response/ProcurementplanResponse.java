package com.example.project.dto.response;

import com.example.project.entity.Procurementplan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementplanResponse {
    private Integer id;
    private String procurementCode;
    private Instant date;
    private Integer requestedBy;
    private String status;
    private Instant approveAt;
    private String note;
    private Integer supplierId;
    private Instant createdAt;

    public static ProcurementplanResponse from(Procurementplan procurementplan) {
        return new ProcurementplanResponse(
                procurementplan.getId(),
                procurementplan.getProcurementCode(),
                procurementplan.getDate(),
                procurementplan.getRequestedBy() != null ? procurementplan.getRequestedBy().getId() : null,
                procurementplan.getStatus(),
                procurementplan.getApproveAt(),
                procurementplan.getNote(),
                procurementplan.getSupplierID() != null ? procurementplan.getSupplierID().getId() : null,
                procurementplan.getCreatedAt()
        );
    }
}