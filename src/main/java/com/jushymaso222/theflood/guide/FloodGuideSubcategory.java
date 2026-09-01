package com.jushymaso222.theflood.guide;

import java.util.List;

public record FloodGuideSubcategory(
        String id,
        String displayName,
        String description,
        int order,
        String requiredMilestone,
        List<FloodGuidePage> pages
) {
    public int pageCount() {
        return pages == null ? 0 : pages.size();
    }

    public boolean isEmpty() {
        return pageCount() == 0;
    }
}
