package com.example.project.config;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import com.example.project.service.SidebarMenuService;
import com.example.project.view.SidebarMenuGroup;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

/**
 * Exposes the common navigation data (current user + sidebar menu) to every Thymeleaf
 * view, so individual controllers don't each have to assemble it.
 *
 * <p>Why an interceptor instead of {@code @ControllerAdvice}: this project keeps its
 * generated JSON {@code @RestController}s in the same package as the MVC controllers, so a
 * package-scoped advice can't cleanly separate them. {@code postHandle} runs only after a
 * handler completes and receives a {@link ModelAndView}; for {@code @ResponseBody}/REST
 * responses that value is {@code null}, so this code naturally adds nothing to API calls
 * and only enriches real HTML view renders.</p>
 *
 * <p>Model attributes added (consumed by {@code fragments/topbar} and {@code fragments/sidebar}):</p>
 * <ul>
 *   <li>{@code currentRole}, {@code currentRoleDisplay}, {@code currentAccountName}, {@code currentBranchId}</li>
 *   <li>{@code allRoles} — for the demo role switcher in the topbar</li>
 *   <li>{@code sidebarMenu} — {@code List<SidebarMenuGroup>} for the current role</li>
 *   <li>{@code activeUrl} — URL of the menu item to highlight for this request</li>
 *   <li>{@code currentRequestUri} — raw request URI (Thymeleaf 3.1 can't read it directly)</li>
 * </ul>
 */
public class SidebarInterceptor implements HandlerInterceptor {

    /** Auth-style views that must NOT show the sidebar/topbar. */
    private static final Set<String> NO_CHROME_VIEWS = Set.of("signin", "signup", "404-error");

    private final SidebarMenuService sidebarMenuService;
    private final CurrentUserContext currentUserContext;

    public SidebarInterceptor(SidebarMenuService sidebarMenuService,
                              CurrentUserContext currentUserContext) {
        this.sidebarMenuService = sidebarMenuService;
        this.currentUserContext = currentUserContext;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // No ModelAndView => REST/@ResponseBody or already-committed response: nothing to render.
        if (modelAndView == null) {
            return;
        }
        String viewName = modelAndView.getViewName();
        if (viewName == null
                || viewName.startsWith("redirect:")
                || viewName.startsWith("forward:")
                || NO_CHROME_VIEWS.contains(viewName)) {
            return;
        }

        String role = currentUserContext.getCurrentRole(request);
        List<SidebarMenuGroup> menu = sidebarMenuService.getMenu(role);
        String uri = request.getRequestURI();

        modelAndView.addObject("currentRole", role);
        modelAndView.addObject("currentRoleDisplay", RoleConstants.displayName(role));
        modelAndView.addObject("currentAccountName", currentUserContext.getCurrentAccountName(request));
        modelAndView.addObject("currentBranchId", currentUserContext.getCurrentBranchId(request));
        modelAndView.addObject("allRoles", RoleConstants.ALL);
        modelAndView.addObject("sidebarMenu", menu);
        modelAndView.addObject("activeUrl", sidebarMenuService.resolveActiveUrl(menu, uri));
        modelAndView.addObject("currentRequestUri", uri);
    }
}
