package com.jushymaso222.theflood.guide;

import java.util.List;

public record ServerSettingsPage(
        String title,
        String subtitle,
        List<ServerSettingEntry> entries
) {
}