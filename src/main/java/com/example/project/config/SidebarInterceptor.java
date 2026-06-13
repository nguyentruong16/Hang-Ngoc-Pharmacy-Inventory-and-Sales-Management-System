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
 * Exposes the common navigation data (current user + sidebar menu) to every Thymeleaf view,
 * so individual controllers don't each have to assemble it.
 *
 * <p>Why an interceptor instead of {@code @ControllerAdvice}: this project keeps its generated
 * JSON {@code @RestController}s in the same package as the MVC controllers, so a package-scoped
 * advice can't cleanly separate them. {@code postHandle} runs only after a handler completes and
 * receives a {@link ModelAndView}; for {@code @ResponseBody}/REST responses that value is
 * {@code null}, so this code naturally adds nothing to API calls and only enriches HTML views.</p>
 *
 * <p>The current user comes from Spring Security via {@link CurrentUserContext}. If there is no
 * authenticated user, no sidebar data is added — an anonymous request never gets a role's menu.</p>
 *
 * <p>Model attributes added (consumed by {@code fragments/topbar} and {@code fragments/sidebar}):
 * {@code currentRole}, {@code currentRoleDisplay}, {@code currentAccountName},
 * {@code currentBranchId}, {@code sidebarMenu}, {@code activeUrl}, {@code currentRequestUri}.</p>
 */
public class SidebarInterceptor implements HandlerInterceptor {

    /** Standalone (no sidebar/topbar) views: auth screens and full-page error/notice pages. */
    private static final Set<String> NO_CHROME_VIEWS = Set.of(
            "signin", "signup", "forgot-password", "reset-password", "change-password", "404-error", "403");

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

        // Only an authenticated user gets a sidebar; never synthesize one for anonymous requests.
        String role = currentUserContext.getCurrentRole();
        if (role == null) {
            return;
        }

        List<SidebarMenuGroup> menu = sidebarMenuService.getMenu(role);
        String uri = request.getRequestURI();

        modelAndView.addObject("currentRole", role);
        modelAndView.addObject("currentRoleDisplay", RoleConstants.vietnameseName(role));
        modelAndView.addObject("currentAccountName", currentUserContext.getCurrentAccountName());
        modelAndView.addObject("currentBranchId", currentUserContext.getCurrentBranchId());
        modelAndView.addObject("sidebarMenu", menu);
        modelAndView.addObject("activeUrl", sidebarMenuService.resolveActiveUrl(menu, uri));
        modelAndView.addObject("currentRequestUri", uri);
    }
}
