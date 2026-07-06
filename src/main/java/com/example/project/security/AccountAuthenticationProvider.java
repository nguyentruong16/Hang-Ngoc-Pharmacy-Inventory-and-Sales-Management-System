package com.example.project.security;

import com.example.project.service.CustomAccountDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {
    private final CustomAccountDetailsService accountDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AccountAuthenticationProvider(
            CustomAccountDetailsService accountDetailsService,
            PasswordEncoder passwordEncoder) {
        this.accountDetailsService = accountDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();

        if (loginId == null || loginId.isBlank() || password.isBlank()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        AccountPrincipal principal;
        try {
            principal = accountDetailsService.loadUserByLoginId(loginId);
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException("Invalid credentials", exception);
        }

        if (!passwordEncoder.matches(password, principal.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UsernamePasswordAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        authenticated.setDetails(authentication.getDetails());
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
