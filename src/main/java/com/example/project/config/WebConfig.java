package com.example.project.config;

import com.example.project.context.CurrentUserContext;
import com.example.project.repository.BranchRepository;
import com.example.project.service.SidebarMenuService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers {@link SidebarInterceptor} so navigation data is available to all HTML views.
 * Static assets are excluded so the interceptor never runs for CSS/JS/images.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SidebarMenuService sidebarMenuService;
    private final CurrentUserContext currentUserContext;
    private final BranchRepository branchRepository;

    public WebConfig(SidebarMenuService sidebarMenuService,
                     CurrentUserContext currentUserContext,
                     BranchRepository branchRepository) {
        this.sidebarMenuService = sidebarMenuService;
        this.currentUserContext = currentUserContext;
        this.branchRepository = branchRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SidebarInterceptor(sidebarMenuService, currentUserContext, branchRepository))
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/error");
    }
}
