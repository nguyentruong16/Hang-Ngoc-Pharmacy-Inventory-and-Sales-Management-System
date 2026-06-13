package com.example.project;

import com.example.project.config.SecurityConfig;
import com.example.project.config.WebConfig;
import com.example.project.context.CurrentUserContext;
import com.example.project.controller.DashboardController;
import com.example.project.controller.PermissionController;
import com.example.project.controller.PlaceholderController;
import com.example.project.security.AccountPrincipal;
import com.example.project.service.CustomAccountDetailsService;
import com.example.project.service.PermissionMatrixService;
import com.example.project.service.SidebarMenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web-slice test (no database) for the real, security-backed navigation. It exercises the
 * SecurityConfig role rules, the SidebarInterceptor, and the Thymeleaf fragments/pages with an
 * authenticated {@link AccountPrincipal} injected per request — verifying both rendering and
 * URL-level role authorization.
 */
@WebMvcTest(controllers = {PermissionController.class, PlaceholderController.class, DashboardController.class})
@Import({SecurityConfig.class, WebConfig.class, SidebarMenuService.class,
        PermissionMatrixService.class, CurrentUserContext.class})
class NavigationRenderingTest {

    @Autowired
    MockMvc mvc;

    // Security auto-config wants a UserDetailsService; mock it (the real one needs JPA repos).
    @MockitoBean
    CustomAccountDetailsService customAccountDetailsService;

    private static RequestPostProcessor as(String role, String displayName, Integer branchId) {
        AccountPrincipal principal = new AccountPrincipal(
                1, displayName, role.toLowerCase(), displayName + "@example.com", "pw", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)), role, branchId);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, "pw", principal.getAuthorities()));
    }

    @Test
    void ownerSeesPermissionTableWithRealChrome() throws Exception {
        mvc.perform(get("/owner/permissions").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/permissions"))
                .andExpect(model().attribute("selectedRole", "OWNER"))
                .andExpect(content().string(containsString("Permission Table")))
                .andExpect(content().string(containsString("Permission Management")))  // owner sidebar group
                .andExpect(content().string(containsString("Olivia Owner")))           // topbar shows real user
                .andExpect(content().string(not(containsString("Demo: act as role")))); // demo switcher gone
    }

    @Test
    void cashierIsForbiddenFromOwnerArea() throws Exception {
        mvc.perform(get("/owner/permissions").with(as("CASHIER", "Cara Cashier", 2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashierSeesOwnRolePage() throws Exception {
        mvc.perform(get("/cashier/customers").with(as("CASHIER", "Cara Cashier", 2)))
                .andExpect(status().isOk())
                .andExpect(view().name("placeholder"))
                .andExpect(content().string(containsString("Customer List")));
    }

    @Test
    void permissionTableHonorsSelectedRoleParam() throws Exception {
        mvc.perform(get("/owner/permissions").param("role", "CASHIER").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRole", "CASHIER"))
                .andExpect(content().string(containsString("Cashier")));
    }

    @Test
    void unauthenticatedUserIsSentToSignin() throws Exception {
        mvc.perform(get("/owner/permissions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void dashboardBridgeRedirectsToRoleDashboard() throws Exception {
        mvc.perform(get("/dashboard").with(as("CASHIER", "Cara Cashier", 2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cashier/dashboard"));
    }
}
