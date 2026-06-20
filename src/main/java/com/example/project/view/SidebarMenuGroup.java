package com.example.project.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * A labelled section of the sidebar (e.g. "Branch Management") containing one or more
 * {@link SidebarMenuItem}s. The first group is conventionally labelled "Main" and holds
 * the role's Dashboard link, mirroring the original static sidebar's grouping.
 */
@Getter
@AllArgsConstructor
public class SidebarMenuGroup {

    private final String label;

    private final String icon;

    private final List<SidebarMenuItem> items;

    private final boolean collapsible;
}
