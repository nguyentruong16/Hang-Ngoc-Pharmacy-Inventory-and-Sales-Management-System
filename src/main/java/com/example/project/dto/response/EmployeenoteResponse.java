package com.example.project.dto.response;

import com.example.project.entity.Employeenote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeenoteResponse {
    private Integer id;
    private Integer accountId;
    private Instant date;
    private String content;

    public static EmployeenoteResponse from(Employeenote employeenote) {
        return new EmployeenoteResponse(
                employeenote.getId(),
                employeenote.getAccountID() != null ? employeenote.getAccountID().getId() : null,
                employeenote.getDate(),
                employeenote.getContent()
        );
    }
}