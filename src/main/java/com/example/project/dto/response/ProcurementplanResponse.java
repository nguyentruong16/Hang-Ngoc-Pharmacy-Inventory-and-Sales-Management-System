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
    private Instant date;
    private Integer branchId;
    private Integer employeeId;
    private Integer statusId;
    private Instant approveAt;
    private String note;

    public static ProcurementplanResponse from(Procurementplan procurementplan) {
        return new ProcurementplanResponse(
                procurementplan.getId(),
                procurementplan.getDate(),
                procurementplan.getBranchID() != null ? procurementplan.getBranchID().getId() : null,
                procurementplan.getEmployeeID() != null ? procurementplan.getEmployeeID().getId() : null,
                procurementplan.getStatusID() != null ? procurementplan.getStatusID().getId() : null,
                procurementplan.getApproveAt(),
                procurementplan.getNote()
        );
    }
}