package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileViewResponse {
    private Integer accountId;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private Boolean status;
}