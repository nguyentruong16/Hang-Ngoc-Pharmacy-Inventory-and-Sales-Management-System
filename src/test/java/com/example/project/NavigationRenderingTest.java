package com.example.project;

import com.example.project.config.WebConfig;
import com.example.project.context.CurrentUserContext;
import com.example.project.controller.PermissionController;
import com.example.project.controller.PlaceholderController;
import com.example.project.service.PermissionMatrixService;
import com.example.project.service.SidebarMenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web-slice test (no database) that actually renders the role-based navigation: it exercises
 * the controllers, the SidebarInterceptor, and the Thymeleaf fragments + new pages. This is
 * what catches fragment/expression mistakes that plain compilation cannot.
 */
@WebMvcTest(controllers = {PermissionController.class, PlaceholderController.class})
@Import({WebConfig.class, SidebarMenuService.class, PermissionMatrixService.class, CurrentUserContext.class})
class NavigationRenderingTest {

    @Autowired
    MockMvc mvc;

    @Test
    void permissionTableRendersWithSidebarAndTopbar() throws Exception {
        mvc.perform(get("/owner/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/permissions"))
                .andExpect(model().attribute("selectedRole", "OWNER"))
                .andExpect(content().string(containsString("Permission Table")))
                .andExpect(content().string(containsString("Read-only")))
                // owner sidebar fragment rendered
                .andExpect(content().string(containsString("Permission Management")))
                // topbar fragment + demo role switcher rendered
                .andExpect(content().string(containsString("Demo: act as role")));
    }

    @Test
    void permissionTableHonorsSelectedRoleParam() throws Exception {
        mvc.perform(get("/owner/permissions").param("role", "CASHIER"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRole", "CASHIER"))
                .andExpect(content().string(containsString("Cashier")));
    }

    @Test
    void placeholderUsesMenuLabelForTitle() throws Exception {
        mvc.perform(get("/cashier/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("placeholder"))
                .andExpect(content().string(containsString("Customer List")))
                .andExpect(content().string(containsString("placeholder for")));
    }

    @Test
    void nonOwnerIsRedirectedAwayFromPermissionTable() throws Exception {
        mvc.perform(get("/owner/permissions")
                        .sessionAttr(CurrentUserContext.CURRENT_ROLE, "CASHIER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cashier/dashboard"));
    }
}
