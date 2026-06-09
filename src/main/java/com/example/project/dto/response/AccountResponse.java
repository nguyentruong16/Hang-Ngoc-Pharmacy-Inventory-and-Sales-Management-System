package com.example.project.dto.response;

import com.example.project.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Integer id;
    private String name;
    private String username;
    private Boolean status;
    private String phoneNumber;
    private String email;

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getUsername(),
                account.getStatus(),
                account.getPhoneNumber(),
                account.getEmail()
        );
    }
}
