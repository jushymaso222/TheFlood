package com.jushymaso222.theflood.client.guide;

public record ServerSettingEntry(
        String section,
        String group,
        String name,
        String value,
        String defaultValue,
        String description
) {

    public boolean isModified() {
        return !value.equals(
                defaultValue
        );
    }
}