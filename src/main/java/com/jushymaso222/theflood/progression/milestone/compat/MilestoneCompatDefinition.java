package com.jushymaso222.theflood.progression.milestone.compat;

import net.minecraft.network.chat.Component;

public record MilestoneCompatDefinition(
        String id,
        String requiredModId,
        Component displayName,
        int sortOrder
) {

    public boolean isAlwaysAvailable() {
        return requiredModId == null
                || requiredModId.isBlank();
    }
}