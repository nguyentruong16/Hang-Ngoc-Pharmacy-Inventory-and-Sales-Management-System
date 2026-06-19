package com.example.project.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class BranchWebAuthenticationDetailsSource
        implements AuthenticationDetailsSource<HttpServletRequest, BranchWebAuthenticationDetails> {

    @Override
    public BranchWebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new BranchWebAuthenticationDetails(context);
    }
}
