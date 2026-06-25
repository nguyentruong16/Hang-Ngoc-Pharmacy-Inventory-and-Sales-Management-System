package com.example.project;

import com.example.project.config.SecurityConfig;
import com.example.project.config.WebConfig;
import com.example.project.context.CurrentUserContext;
import com.example.project.controller.DashboardController;
import com.example.project.controller.PermissionController;
import com.example.project.controller.PlaceholderController;
import com.example.project.entity.Branch;
import com.example.project.repository.BranchRepository;
import com.example.project.security.AccountPrincipal;
import com.example.project.security.BranchAwareAuthenticationProvider;
import com.example.project.security.BranchWebAuthenticationDetailsSource;
import com.example.project.service.CustomAccountDetailsService;
import com.example.project.service.OwnerPermissionService;
import com.example.project.service.SidebarMenuService;
import com.example.project.view.BranchPermissionRow;
import com.example.project.view.PermissionBranchOption;
import com.example.project.view.PermissionPageView;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
 * URL-level role authorization. The DB-backed {@link OwnerPermissionService} is mocked; by
 * default it returns an empty {@link PermissionPageView} so the page renders without a database.
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

    // PermissionController depends on this DB-backed service; mock returns an empty page by default.
    @MockitoBean
    OwnerPermissionService ownerPermissionService;

    @BeforeEach
    void setUp() {
        when(branchRepository.findById(any())).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            Branch branch = new Branch();
            branch.setId(id);
            branch.setName("Branch " + id);
            return Optional.of(branch);
        });
        when(branchRepository.findAllWithStatus()).thenReturn(List.of());

        // Default: a valid empty page so the Thymeleaf template renders without NPEs.
        when(ownerPermissionService.getPermissionPage(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyPage());
    }

    private static PermissionPageView emptyPage() {
        return new PermissionPageView(List.of(), null, List.of(), 0, 10, 0L, 0, null, null);
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
                .andExpect(content().string(containsString("Bảng phân quyền")))           // title + sidebar link
                .andExpect(content().string(containsString("Chọn chi nhánh")))            // branch button area
                .andExpect(content().string(containsString("Chưa có chi nhánh nào.")))    // empty branch state
                .andExpect(content().string(containsString("Olivia Owner")))             // topbar shows real user
                .andExpect(content().string(not(containsString("Thêm phân quyền"))))      // add form removed
                .andExpect(content().string(not(containsString("Demo: act as role"))));   // demo switcher gone
    }

    @Test
    void ownerPageRendersBranchTableAndDropdownOptions() throws Exception {
        PermissionBranchOption branch =
                new PermissionBranchOption(2, "Hằng Ngọc 2", "Đang hoạt động", true, true, "Đang hoạt động");
        BranchPermissionRow row = new BranchPermissionRow(
                1, "Nguyễn Văn A", "pharmacist01", "PHARMACIST", "Dược sĩ", false, true);
        PermissionPageView pageView = new PermissionPageView(
                List.of(branch), branch, List.of(row), 0, 10, 1L, 1, null, null);

        when(ownerPermissionService.getPermissionPage(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageView);

        mvc.perform(get("/owner/permissions").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nguyễn Văn A")))    // account row
                .andExpect(content().string(containsString("pharmacist01")))    // username/email column
                .andExpect(content().string(containsString("Hằng Ngọc 2")))     // branch button label
                .andExpect(content().string(containsString("Dược sĩ")))         // role dropdown option
                .andExpect(content().string(containsString("-- Chưa phân quyền --"))); // empty option on editable row
    }

    @Test
    void inactiveBranchShowsReadOnlyNotice() throws Exception {
        PermissionBranchOption inactive =
                new PermissionBranchOption(3, "Hằng Ngọc 3", "Ngừng hoạt động", false, true, "Đã ngừng hoạt động");
        BranchPermissionRow row = new BranchPermissionRow(
                4, "Trần Thị B", "cashier04", "CASHIER", "Thu ngân", false, false); // not editable
        PermissionPageView pageView = new PermissionPageView(
                List.of(inactive), inactive, List.of(row), 0, 10, 1L, 1, null, null);

        when(ownerPermissionService.getPermissionPage(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageView);

        mvc.perform(get("/owner/permissions").param("branchId", "3").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đã ngừng hoạt động")))   // inactive branch badge
                .andExpect(content().string(containsString(
                        "Chi nhánh này đã ngừng hoạt động. Không thể chỉnh sửa phân quyền.")))  // notice
                .andExpect(content().string(containsString("Thu ngân")))             // read-only role text
                // A non-editable row must NOT render the auto-submitting dropdown.
                .andExpect(content().string(not(containsString("this.form.submit()"))));
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
    void permissionPageEchoesSearch() throws Exception {
        mvc.perform(get("/owner/permissions").param("search", "pharmacist01").with(as("OWNER", "Olivia Owner", 1)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "pharmacist01"));
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
