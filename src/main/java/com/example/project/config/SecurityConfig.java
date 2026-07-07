package com.example.project.config;

import com.example.project.constant.RoleConstants;
import com.example.project.security.AccountAuthenticationProvider;
import com.example.project.security.ReasonAwareSessionExpiredStrategy;
import com.example.project.security.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SessionRegistry sessionRegistry,
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler,
            ReasonAwareSessionExpiredStrategy sessionExpiredStrategy,
            AccountAuthenticationProvider accountAuthenticationProvider) throws Exception {
        http
                .authenticationProvider(accountAuthenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        // Public (no login required)
                        .requestMatchers(
                                "/signin",
                                "/forgot-password",
                                "/reset-password",
                                "/error",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Role areas: each role tree is reachable only by that role. Hiding the
                        // sidebar item is not enough — this blocks direct URL access too.
                        .requestMatchers("/owner/**").hasRole(RoleConstants.OWNER)
                        .requestMatchers("/pharmacist/**").hasRole(RoleConstants.PHARMACIST)
                        .requestMatchers("/accountant/**").hasRole(RoleConstants.ACCOUNTANT)
                        // Shared Customer module: reachable by Owner and Pharmacist —
                        // not Accountant. Lives outside the role-prefixed trees.
                        .requestMatchers("/customer/**").hasAnyRole(
                                RoleConstants.OWNER,
                                RoleConstants.PHARMACIST)
                        // Everything else (/, /dashboard, /profile, /change-password, /403, REST APIs)
                        // requires only that the user is signed in.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/signin")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        // Land on the user's own role dashboard, not a single shared page.
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/signin?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionAuthenticationErrorUrl("/signin?sessionLimit")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredSessionStrategy(sessionExpiredStrategy)
                        .sessionRegistry(sessionRegistry)
                )
                // Authenticated user without the required role -> friendly 403 page.
                .exceptionHandling(exception -> exception.accessDeniedPage("/403"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new RoleBasedAuthenticationSuccessHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String failureUrl = exception instanceof SessionAuthenticationException
                    || exception instanceof AuthenticationServiceException
                    && exception.getCause() instanceof SessionAuthenticationException
                    ? "/signin?sessionLimit"
                    : "/signin?error";
            response.sendRedirect(request.getContextPath() + failureUrl);
        };
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
