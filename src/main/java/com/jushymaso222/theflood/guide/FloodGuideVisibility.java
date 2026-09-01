package com.jushymaso222.theflood.guide;

import com.jushymaso222.theflood.progression.milestone.client.ClientMilestoneData;
import net.minecraft.resources.ResourceLocation;

public final class FloodGuideVisibility {

    private FloodGuideVisibility() {
    }

    public static boolean isVisible(
            FloodGuideCategory category
    ) {
        return isUnlocked(
                category.requiredMilestone()
        );
    }

    public static boolean isVisible(
            FloodGuideSubcategory subcategory
    ) {
        return isUnlocked(
                subcategory.requiredMilestone()
        );
    }

    public static boolean isVisible(
            FloodGuidePage page
    ) {
        return isUnlocked(
                page.requiredMilestone()
        );
    }

    private static boolean isUnlocked(
            String milestoneId
    ) {
        if (milestoneId == null
                || milestoneId.isBlank()) {
            return true;
        }

        try {
            return ClientMilestoneData.isCompleted(
                    new ResourceLocation(
                            milestoneId
                    )
            );
        } catch (Exception ignored) {
            return false;
        }
    }
}