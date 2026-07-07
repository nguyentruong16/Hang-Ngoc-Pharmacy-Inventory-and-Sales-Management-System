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
    private String procurementPlanCode;
    private Instant date;
    private Integer employeeId;
    private String status;
    private Instant approveAt;
    private String note;

    public static ProcurementplanResponse from(Procurementplan procurementplan) {
        return new ProcurementplanResponse(
                procurementplan.getId(),
                procurementplan.getProcurementPlanCode(), 
                procurementplan.getDate(),
                procurementplan.getEmployeeID() != null ? procurementplan.getEmployeeID().getId() : null,
                procurementplan.getStatus(),
                procurementplan.getApproveAt(),
                procurementplan.getNote()
        );
    }
}