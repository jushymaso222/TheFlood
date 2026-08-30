package com.jushymaso222.theflood.progression.milestone;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public record MilestoneSnapshot(
        int progressionValue,
        List<ResourceLocation> achieved,
        List<ResourceLocation> completionHistory
) {
}