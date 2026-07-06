package com.example.project.dto.response;

import com.example.project.entity.Accountpermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountpermissionResponse {
    private Integer id;
    private Integer accountId;
    private String role;

    public static AccountpermissionResponse from(Accountpermission accountpermission) {
        return new AccountpermissionResponse(
                accountpermission.getId(),
                accountpermission.getAccountID() != null ? accountpermission.getAccountID().getId() : null,
                accountpermission.getRole()
        );
    }
}