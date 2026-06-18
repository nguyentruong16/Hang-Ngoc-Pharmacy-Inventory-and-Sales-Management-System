package com.example.project.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class BranchWebAuthenticationDetails extends WebAuthenticationDetails {
    private final Integer branchId;

    public BranchWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.branchId = parseBranchId(request.getParameter("branchId"));
    }

    public Integer getBranchId() {
        return branchId;
    }

    private Integer parseBranchId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
