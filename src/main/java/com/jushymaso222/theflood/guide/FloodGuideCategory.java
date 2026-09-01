package com.jushymaso222.theflood.guide;

import java.util.List;

public record FloodGuideCategory(
        String id,
        String displayName,
        String description,
        int order,
        String requiredMilestone,
        List<FloodGuidePage> pages,
        List<FloodGuideSubcategory> subcategories
) {
    public int pageCount() {
        return pages == null ? 0 : pages.size();
    }

    public int subcategoryCount() {
        return subcategories == null ? 0 : subcategories.size();
    }

    public boolean isEmpty() {
        return pageCount() == 0 && subcategoryCount() == 0;
    }
}
