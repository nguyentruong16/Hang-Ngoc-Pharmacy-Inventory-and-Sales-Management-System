package com.example.project.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A single clickable entry in the sidebar.
 *
 * <p>This is a lightweight view model (not a JPA entity and not persisted). Whether
 * the item is "active" is intentionally NOT stored here — it depends on the current
 * request URI and is resolved per request by comparing {@link #url} against the
 * {@code activeUrl} model attribute in the Thymeleaf fragment.</p>
 */
@Getter
@AllArgsConstructor
public class SidebarMenuItem {

    /** Visible text, e.g. "Branch List". */
    private final String label;

    /** Target URL, e.g. "/owner/branches". Used both for the link and for active matching. */
    private final String url;

    /** Tabler icon class, e.g. "ti ti-building-store" (icon font ships in the existing bundle). */
    private final String icon;

    /** Owning group label, surfaced on placeholder pages as the "module". */
    private final String module;
}
