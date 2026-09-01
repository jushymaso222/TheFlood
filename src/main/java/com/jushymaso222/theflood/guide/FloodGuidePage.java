package com.jushymaso222.theflood.guide;

public record FloodGuidePage(
        String id,
        String title,
        int order,
        String requiredMilestone,
        String body
) {
}
