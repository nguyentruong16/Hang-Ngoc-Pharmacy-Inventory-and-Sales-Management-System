package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerUserRowResponse {

    private Integer accountId;
    private String employeeCode;
    private String name;
    private String username;
    private String roleDisplay;
    private String branchNames;
    private String phoneNumber;
    private String email;
    private Boolean status;
    private String statusDisplay;
    private Boolean canDeactivate;
    private Boolean canActivate;
}