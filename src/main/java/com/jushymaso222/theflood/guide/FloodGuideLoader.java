package com.jushymaso222.theflood.guide;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FloodGuideLoader {

    private static final String ROOT = "guide";

    private static List<FloodGuideCategory> categories = List.of();

    private FloodGuideLoader() {
    }

    public static void reload(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> jsonResources =
                resourceManager.listResources(
                        ROOT,
                        location -> location.getPath().endsWith(".json")
                );

        Map<String, CategoryBuilder> categoryBuilders = new HashMap<>();

        /*
         * Pass 1:
         * guide/<category>/category.json
         */
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources.entrySet()) {
            String[] parts = relativeParts(entry.getKey());

            if (parts.length != 2 || !parts[1].equals("category.json")) {
                continue;
            }

            JsonObject json = readJson(entry.getValue(), entry.getKey());
            if (json == null) {
                continue;
            }

            String folderId = parts[0];

            categoryBuilders.put(
                    folderId,
                    new CategoryBuilder(
                            getString(json, "id", folderId),
                            getString(json, "title", prettify(folderId)),
                            getString(json, "description", ""),
                            getInt(json, "order", 0),
                            getNullableString(json, "requires_milestone")
                    )
            );
        }

        /*
         * Pass 2:
         * guide/<category>/<subcategory>/subcategory.json
         */
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources.entrySet()) {
            String[] parts = relativeParts(entry.getKey());

            if (parts.length != 3 || !parts[2].equals("subcategory.json")) {
                continue;
            }

            CategoryBuilder category = categoryBuilders.get(parts[0]);
            if (category == null) {
                continue;
            }

            JsonObject json = readJson(entry.getValue(), entry.getKey());
            if (json == null) {
                continue;
            }

            String folderId = parts[1];

            category.subcategories.put(
                    folderId,
                    new SubcategoryBuilder(
                            getString(json, "id", folderId),
                            getString(json, "title", prettify(folderId)),
                            getString(json, "description", ""),
                            getInt(json, "order", 0),
                            getNullableString(json, "requires_milestone")
                    )
            );
        }

        /*
         * Pass 3A:
         * Direct category articles:
         *
         * guide/<category>/<article>.json
         *
         * category.json itself is ignored here.
         */
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources.entrySet()) {
            String[] parts = relativeParts(entry.getKey());

            if (parts.length != 2 || parts[1].equals("category.json")) {
                continue;
            }

            CategoryBuilder category = categoryBuilders.get(parts[0]);
            if (category == null) {
                continue;
            }

            FloodGuidePage page = readPage(
                    entry.getValue(),
                    entry.getKey(),
                    parts[1]
            );

            if (page != null) {
                category.pages.add(page);
            }
        }

        /*
         * Pass 3B:
         * Subcategory articles:
         *
         * guide/<category>/<subcategory>/<article>.json
         *
         * A folder only counts as a subcategory if it contains
         * subcategory.json.
         */
        for (Map.Entry<ResourceLocation, Resource> entry : jsonResources.entrySet()) {
            String[] parts = relativeParts(entry.getKey());

            if (parts.length != 3 || parts[2].equals("subcategory.json")) {
                continue;
            }

            CategoryBuilder category = categoryBuilders.get(parts[0]);
            if (category == null) {
                continue;
            }

            SubcategoryBuilder subcategory =
                    category.subcategories.get(parts[1]);

            if (subcategory == null) {
                continue;
            }

            FloodGuidePage page = readPage(
                    entry.getValue(),
                    entry.getKey(),
                    parts[2]
            );

            if (page != null) {
                subcategory.pages.add(page);
            }
        }

        List<FloodGuideCategory> loadedCategories = new ArrayList<>();

        for (CategoryBuilder category : categoryBuilders.values()) {
            category.pages.sort(PAGE_ORDER);

            List<FloodGuideSubcategory> loadedSubcategories = new ArrayList<>();

            for (SubcategoryBuilder subcategory : category.subcategories.values()) {
                subcategory.pages.sort(PAGE_ORDER);

                loadedSubcategories.add(
                        new FloodGuideSubcategory(
                                subcategory.id,
                                subcategory.title,
                                subcategory.description,
                                subcategory.order,
                                subcategory.requiredMilestone,
                                List.copyOf(subcategory.pages)
                        )
                );
            }

            loadedSubcategories.sort(
                    Comparator.comparingInt(FloodGuideSubcategory::order)
                            .thenComparing(FloodGuideSubcategory::displayName)
            );

            loadedCategories.add(
                    new FloodGuideCategory(
                            category.id,
                            category.title,
                            category.description,
                            category.order,
                            category.requiredMilestone,
                            List.copyOf(category.pages),
                            List.copyOf(loadedSubcategories)
                    )
            );
        }

        loadedCategories.sort(
                Comparator.comparingInt(FloodGuideCategory::order)
                        .thenComparing(FloodGuideCategory::displayName)
        );

        categories = List.copyOf(loadedCategories);
    }

    public static List<FloodGuideCategory> getCategories() {
        return categories;
    }

    public static FloodGuideCategory getCategory(String id) {
        if (id == null) {
            return null;
        }

        for (FloodGuideCategory category : categories) {
            if (category.id().equals(id)) {
                return category;
            }
        }

        return null;
    }

    public static FloodGuideSubcategory getSubcategory(
            FloodGuideCategory category,
            String id
    ) {
        if (category == null || id == null) {
            return null;
        }

        for (FloodGuideSubcategory subcategory : category.subcategories()) {
            if (subcategory.id().equals(id)) {
                return subcategory;
            }
        }

        return null;
    }

    private static FloodGuidePage readPage(
            Resource resource,
            ResourceLocation location,
            String filename
    ) {
        JsonObject json = readJson(resource, location);
        if (json == null) {
            return null;
        }

        String fallbackId = filename.endsWith(".json")
                ? filename.substring(0, filename.length() - 5)
                : filename;

        return new FloodGuidePage(
                getString(json, "id", fallbackId),
                getString(json, "title", prettify(fallbackId)),
                getInt(json, "order", 0),
                getNullableString(json, "requires_milestone"),
                getString(json, "body", "")
        );
    }

    private static String getNullableString(
            JsonObject json,
            String key
    ) {
        if (!json.has(key)
                || json.get(key).isJsonNull()) {
            return null;
        }

        String value = json.get(key).getAsString();

        return value.isBlank()
                ? null
                : value;
    }

    private static final Comparator<FloodGuidePage> PAGE_ORDER =
            Comparator.comparingInt(FloodGuidePage::order)
                    .thenComparing(FloodGuidePage::title);

    private static String[] relativeParts(ResourceLocation location) {
        String path = location.getPath();

        if (!path.startsWith(ROOT + "/")) {
            return new String[0];
        }

        return path.substring((ROOT + "/").length()).split("/");
    }

    private static JsonObject readJson(
            Resource resource,
            ResourceLocation location
    ) {
        try (BufferedReader reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception exception) {
            System.err.println(
                    "[The Flood] Failed to load guide resource "
                            + location
                            + ": "
                            + exception.getMessage()
            );
            return null;
        }
    }

    private static String getString(
            JsonObject json,
            String key,
            String fallback
    ) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsString()
                : fallback;
    }

    private static int getInt(
            JsonObject json,
            String key,
            int fallback
    ) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsInt()
                : fallback;
    }

    private static String prettify(String id) {
        String[] words = id.replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0)));

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }

    private static final class CategoryBuilder {
        private final String id;
        private final String title;
        private final String description;
        private final int order;
        private final String requiredMilestone;

        private final List<FloodGuidePage> pages = new ArrayList<>();
        private final Map<String, SubcategoryBuilder> subcategories = new HashMap<>();

        private CategoryBuilder(
                String id,
                String title,
                String description,
                int order,
                String requiredMilestone
        ) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.order = order;
            this.requiredMilestone = requiredMilestone;
        }
    }

    private static final class SubcategoryBuilder {
        private final String id;
        private final String title;
        private final String description;
        private final int order;
        private final String requiredMilestone;
        private final List<FloodGuidePage> pages = new ArrayList<>();

        private SubcategoryBuilder(
                String id,
                String title,
                String description,
                int order,
                String requiredMilestone
        ) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.order = order;
            this.requiredMilestone = requiredMilestone;
        }
    }
}
