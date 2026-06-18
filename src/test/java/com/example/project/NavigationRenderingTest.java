package com.example.project;

import com.example.project.config.SecurityConfig;
import com.example.project.config.WebConfig;
import com.example.project.context.CurrentUserContext;
import com.example.project.controller.DashboardController;
import com.example.project.controller.PermissionController;
import com.example.project.controller.PlaceholderController;
import com.example.project.entity.Account;
import com.example.project.entity.Branch;
import com.example.project.repository.BranchRepository;
import com.example.project.security.AccountPrincipal;
import com.example.project.security.BranchAwareAuthenticationProvider;
import com.example.project.security.BranchWebAuthenticationDetailsSource;
import com.example.project.service.CustomAccountDetailsService;
import com.example.project.service.OwnerPermissionService;
import com.example.project.service.SidebarMenuService;
import com.example.project.view.PermissionAssignmentRow;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
 * URL-level role authorization. The DB-backed {@link OwnerPermissionService} is mocked (it
 * returns empty collections by default, which is enough to render the page).
 */
@WebMvcTest(controllers = {PermissionController.class, PlaceholderController.class, DashboardController.class})
@Import({
        SecurityConfig.class,
        WebConfig.class,
        SidebarMenuService.class,
        CurrentUserContext.class,
        BranchWebAuthenticationDetailsSource.class
})
class NavigationRenderingTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CustomAccountDetailsService customAccountDetailsService;

    @MockitoBean
    BranchAwareAuthenticationProvider branchAwareAuthenticationProvider;

    @MockitoBean
    BranchRepository branchRepository;

    // PermissionController depends on this DB-backed service; mock returns empty lists by default.
    @MockitoBean
    OwnerPermissionService ownerPermissionService;

    @BeforeEach
    void setUpBranchRepository() {
        when(branchRepository.findById(any())).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            Branch branch = new Branch();
            branch.setId(id);
            branch.setName("Branch " + id);
            return Optional.of(branch);
        });
        when(branchRepository.findAllWithStatus()).thenReturn(List.of());
    }

    private static RequestPostProcessor as(String role, String displayName, Integer branchId) {
        AccountPrincipal principal = new AccountPrincipal(
                1, displayName, role.toLowerCase(), displayName + "@example.com", "pw", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)), role, branchId);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, "pw", principal.getAuthorities()));
    }

    @Test
    void ownerSeesRoleByBranchPageWithRealChrome() throws Exception {
        mvc.perform(get("/owner/permissions").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/permissions"))
                .andExpect(content().string(containsString("Phân quyền theo chi nhánh")))  // page title + sidebar link
                .andExpect(content().string(containsString("Thêm phân quyền")))            // add form
                .andExpect(content().string(containsString("Olivia Owner")))               // topbar shows real user
                .andExpect(content().string(not(containsString("Demo: act as role"))));    // demo switcher gone
    }

    @Test
    void ownerPageRendersAssignmentRowsAndDropdownOptions() throws Exception {
        Account account = new Account();
        account.setId(1);
        account.setName("Nguyễn Văn A");
        account.setUsername("pharmacist01");
        account.setEmail("a@example.com");
        Branch branch = new Branch();
        branch.setId(2);
        branch.setName("Hằng Ngọc 2");
        when(ownerPermissionService.listAccounts()).thenReturn(List.of(account));
        when(ownerPermissionService.listBranches()).thenReturn(List.of(branch));
        when(ownerPermissionService.listAssignments(any(), any(), any())).thenReturn(List.of(
                new PermissionAssignmentRow(10, 1, "Nguyễn Văn A", "pharmacist01", "a@example.com",
                        2, "Hằng Ngọc 2", "PHARMACIST", "Dược sĩ")));

        mvc.perform(get("/owner/permissions").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nguyễn Văn A")))   // assignment row + account option
                .andExpect(content().string(containsString("pharmacist01")))   // username column + option label
                .andExpect(content().string(containsString("Hằng Ngọc 2")))    // branch column + option
                .andExpect(content().string(containsString("Dược sĩ")));       // role display badge
    }

    @Test
    void cashierIsForbiddenFromOwnerArea() throws Exception {
        mvc.perform(get("/owner/permissions").with(as("CASHIER", "Cara Cashier", 2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashierSeesOwnRolePageWithVietnameseLabel() throws Exception {
        mvc.perform(get("/cashier/customers").with(as("CASHIER", "Cara Cashier", 2)))
                .andExpect(status().isOk())
                .andExpect(view().name("placeholder"))
                .andExpect(content().string(containsString("Khách hàng")));  // translated menu label
    }

    @Test
    void permissionPageEchoesRoleFilter() throws Exception {
        mvc.perform(get("/owner/permissions").param("role", "CASHIER").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filterRole", "CASHIER"));
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
