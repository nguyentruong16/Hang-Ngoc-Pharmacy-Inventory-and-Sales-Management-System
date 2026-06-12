package com.example.project.controller;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TEMPORARY testing aid — lets you preview the role-based sidebar and the Permission Table
 * without a real login.
 *
 * <p>This is NOT authentication. It only writes the same session attributes the future login
 * module is expected to set ({@code currentRole}, {@code currentAccountName}). Once the real
 * login is integrated, this controller can be deleted with no other changes. It is reachable
 * from the "Demo: act as role" section of the topbar user dropdown.</p>
 *
 * @see CurrentUserContext for the session attribute contract
 */
@Controller
public class DevRoleController {

    /** Switch the acting role and land on that role's dashboard. */
    @GetMapping("/dev/act-as")
    public String actAs(@RequestParam("role") String role, HttpServletRequest request) {
        String target = RoleConstants.isValid(role) ? role : RoleConstants.DEFAULT_ROLE;
        request.getSession(true).setAttribute(CurrentUserContext.CURRENT_ROLE, target);
        request.getSession().setAttribute(CurrentUserContext.CURRENT_ACCOUNT_NAME,
                RoleConstants.displayName(target) + " (demo)");
        return "redirect:" + RoleConstants.dashboardPath(target);
    }

    /** Clear the demo role (simulates "logged out" — falls back to the default role). */
    @GetMapping("/dev/reset-role")
    public String resetRole(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession().removeAttribute(CurrentUserContext.CURRENT_ROLE);
            request.getSession().removeAttribute(CurrentUserContext.CURRENT_ACCOUNT_NAME);
        }
        return "redirect:" + RoleConstants.dashboardPath(RoleConstants.DEFAULT_ROLE);
    }
}
