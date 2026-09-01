package com.jushymaso222.theflood.guide.client;

import com.jushymaso222.theflood.guide.FloodGuideCategory;
import com.jushymaso222.theflood.guide.FloodGuidePage;
import com.jushymaso222.theflood.guide.FloodGuideLoader;
import com.jushymaso222.theflood.guide.FloodGuideSubcategory;
import com.jushymaso222.theflood.guide.ServerSettingEntry;
import com.jushymaso222.theflood.guide.ServerSettingsPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import net.minecraft.client.gui.components.EditBox;
import com.jushymaso222.theflood.guide.FloodGuideVisibility;
import com.jushymaso222.theflood.guide.network.RequestServerSettingsPacket;
import com.jushymaso222.theflood.network.FloodNetwork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FloodGuideScreen extends Screen {
    private final Screen parent;

    /*
     * Server Settings are live server data, not static guide content.
     * Keeping this as a synthetic category preserves the current UI exactly
     * without putting it into assets/theflood/guide/.
     */
    private static final FloodGuideCategory SERVER_SETTINGS_CATEGORY =
                new FloodGuideCategory(
                        "__server_settings",
                        "Server Settings",
                        "Current Flood configuration for this server.",
                        Integer.MAX_VALUE,
                        null,
                        List.of(),
                        List.of()
                );

    private View view = View.HOME;
    private FloodGuideCategory selectedCategory;
    private FloodGuideSubcategory selectedSubcategory;
    private int selectedPage;
    private int selectedServerSettingsPage;
    private int browserPage;

    private final List<RecentEntry> recentEntries = new ArrayList<>();
    private final List<CardHitbox> cardHitboxes = new ArrayList<>();

    private static final int MAX_PANEL_WIDTH = 670;
    private static final int MAX_PANEL_HEIGHT = 400;
    private static final int SCREEN_MARGIN = 18;
    private static final int PANEL_PADDING = 14;
    private static final int HEADER_HEIGHT = 48;

    private static final int COLOR_SCREEN_OVERLAY = 0xA6000000;
    private static final int COLOR_PANEL = 0xF2181818;
    private static final int COLOR_PANEL_INSET = 0xEB202020;
    private static final int COLOR_CARD = 0xE92A2A2A;
    private static final int COLOR_CARD_HOVER = 0xF0363636;
    private static final int COLOR_CARD_DARK = 0xEB151515;
    private static final int COLOR_BORDER = 0xFF4A4A4A;
    private static final int COLOR_ACCENT = 0xFF43D9FF;
    private static final int COLOR_TEXT = 0xFFF2F2F2;
    private static final int COLOR_TEXT_SECONDARY = 0xFFB7B7B7;
    private static final int COLOR_TEXT_MUTED = 0xFF777777;
    private static final int COLOR_MODIFIED = 0xFFFFC857;

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentTop;
    private int contentRight;
    private int contentBottom;

    private Rect backRect;
    private Rect homeRect;
    private Rect startHereRect;
    private Rect searchRect;

    private EditBox searchBox;

        private final List<SearchResult> searchResults =
                new ArrayList<>();

    public FloodGuideScreen(Screen parent) {
        super(Component.literal("The Flood Guide"));
        this.parent = parent;
    }

    private List<FloodGuideCategory> getVisibleCategories() {
    return FloodGuideLoader.getCategories()
            .stream()
            .filter(FloodGuideVisibility::isVisible)
            .toList();
}

private List<FloodGuidePage> getVisiblePages(
        FloodGuideCategory category
) {
    if (category == null) {
        return List.of();
    }

    return category.pages()
            .stream()
            .filter(FloodGuideVisibility::isVisible)
            .toList();
}

private List<FloodGuideSubcategory> getVisibleSubcategories(
        FloodGuideCategory category
) {
    if (category == null) {
        return List.of();
    }

    return category.subcategories()
            .stream()
            .filter(FloodGuideVisibility::isVisible)
            .toList();
}

private List<FloodGuidePage> getVisiblePages(
        FloodGuideSubcategory subcategory
) {
    if (subcategory == null) {
        return List.of();
    }

    return subcategory.pages()
            .stream()
            .filter(FloodGuideVisibility::isVisible)
            .toList();
}

    @Override
    protected void init() {
        super.init();

        if (!ClientServerSettingsData.hasReceivedSettings()) {
                FloodNetwork.CHANNEL.sendToServer(
                        new RequestServerSettingsPacket()
                );
                }

        FloodGuideLoader.reload(
                Minecraft.getInstance().getResourceManager()
        );

        searchBox = new EditBox(
                font,
                0,
                0,
                100,
                20,
                Component.literal("Search guide")
        );

        searchBox.setMaxLength(80);
        searchBox.setBordered(false);
        searchBox.setTextColor(COLOR_TEXT);
        searchBox.setTextColorUneditable(COLOR_TEXT_MUTED);

        searchBox.setResponder(value -> updateSearchResults());

        addRenderableWidget(searchBox);

        calculateLayout();
    }

    private void calculateLayout() {
        int availableWidth = Math.max(320, width - SCREEN_MARGIN * 2);
        int availableHeight = Math.max(230, height - SCREEN_MARGIN * 2);

        panelWidth = Math.min(MAX_PANEL_WIDTH, availableWidth);
        panelHeight = Math.min(MAX_PANEL_HEIGHT, availableHeight);

        panelWidth = Math.min(panelWidth, Math.max(1, width - 4));
        panelHeight = Math.min(panelHeight, Math.max(1, height - 4));

        panelLeft = width / 2 - panelWidth / 2;
        panelTop = height / 2 - panelHeight / 2;

        contentLeft = panelLeft + PANEL_PADDING;
        contentTop = panelTop + HEADER_HEIGHT;
        contentRight = panelLeft + panelWidth - PANEL_PADDING;
        contentBottom = panelTop + panelHeight - PANEL_PADDING;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(0, 0, width, height, COLOR_SCREEN_OVERLAY);

        calculateLayout();
        cardHitboxes.clear();

        renderPanel(graphics);
        renderHeader(graphics, mouseX, mouseY);

        if (searchBox != null) {
                searchBox.setVisible(view == View.HOME);
        }

        switch (view) {
            case HOME -> renderHome(graphics, mouseX, mouseY);
            case CATEGORIES -> renderCategories(graphics, mouseX, mouseY);
            case CATEGORY -> renderCategory(graphics, mouseX, mouseY);
            case SUBCATEGORY -> renderSubcategory(graphics, mouseX, mouseY);
            case ARTICLE -> {
                if (selectedCategory == SERVER_SETTINGS_CATEGORY) {
                    renderServerSettingsArticle(graphics, mouseX, mouseY);
                } else {
                    renderArticle(graphics, mouseX, mouseY);
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics graphics) {
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, COLOR_PANEL);
        drawBorder(graphics, panelLeft, panelTop, panelWidth, panelHeight, COLOR_BORDER);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 2, COLOR_ACCENT);
    }

    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = panelLeft + PANEL_PADDING;
        int titleY = panelTop + 12;

        graphics.drawString(font, "THE FLOOD GUIDE", titleX, titleY, COLOR_TEXT, false);
        graphics.fill(titleX, titleY + 11, titleX + font.width("THE FLOOD GUIDE"), titleY + 12, COLOR_ACCENT);

        backRect = null;
        homeRect = null;

        if (view != View.HOME) {
            backRect = new Rect(titleX, panelTop + 29, 42, 14);
            graphics.drawString(
                    font,
                    "< BACK",
                    backRect.x(),
                    backRect.y() + 2,
                    backRect.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT_SECONDARY,
                    false
            );

            homeRect = new Rect(panelLeft + panelWidth - PANEL_PADDING - 35, panelTop + 10, 35, 18);
            drawTextButton(graphics, "HOME", homeRect, homeRect.contains(mouseX, mouseY));
        }

        String breadcrumb = buildBreadcrumb();
        if (!breadcrumb.isBlank()) {
            int breadcrumbX = titleX + 55;
            int maxWidth = Math.max(20, panelLeft + panelWidth - PANEL_PADDING - breadcrumbX - 45);
            graphics.drawString(
                    font,
                    trimToWidth(breadcrumb, maxWidth),
                    breadcrumbX,
                    panelTop + 31,
                    COLOR_TEXT_MUTED,
                    false
            );
        }
    }

    private String buildBreadcrumb() {
        return switch (view) {
            case HOME -> "";
            case CATEGORIES -> "HOME > CATEGORIES";
            case CATEGORY -> selectedCategory == null
                    ? "HOME > CATEGORIES"
                    : "HOME > " + selectedCategory.displayName().toUpperCase();
            case SUBCATEGORY -> selectedCategory == null || selectedSubcategory == null
                    ? ""
                    : selectedCategory.displayName().toUpperCase()
                    + " > "
                    + selectedSubcategory.displayName().toUpperCase();
            case ARTICLE -> {
                if (selectedCategory == SERVER_SETTINGS_CATEGORY) {
                    yield "SERVER SETTINGS > " + getSelectedServerSettingsTitle().toUpperCase();
                }

                FloodGuidePage page = getSelectedPage();
                if (selectedSubcategory == null || page == null) {
                    yield "";
                }

                yield selectedSubcategory.displayName().toUpperCase()
                        + " > "
                        + page.title().toUpperCase();
            }
        };
    }

    private void renderHome(GuiGraphics graphics, int mouseX, int mouseY) {
        int gap = 10;
        int totalWidth = contentRight - contentLeft;
        int leftWidth = Math.max(130, (totalWidth - gap) * 47 / 100);
        int rightWidth = totalWidth - leftWidth - gap;
        int leftX = contentLeft;
        int rightX = leftX + leftWidth + gap;

        int startHeight = Math.min(100, Math.max(78, (contentBottom - contentTop) / 3));
        startHereRect = new Rect(leftX, contentTop, leftWidth, startHeight);

        boolean startHover = startHereRect.contains(mouseX, mouseY);
        drawCard(graphics, startHereRect, startHover);

        graphics.drawString(font, "START HERE", startHereRect.x() + 10, startHereRect.y() + 10, COLOR_ACCENT, false);
        graphics.drawString(font, "New to The Flood?", startHereRect.x() + 10, startHereRect.y() + 28, COLOR_TEXT, false);

        renderWrappedText(
                graphics,
                "Learn the core systems before the world starts trying to eat you.",
                startHereRect.x() + 10,
                startHereRect.y() + 44,
                startHereRect.width() - 20,
                COLOR_TEXT_SECONDARY,
                3,
                startHereRect.y() + startHereRect.height() - 20
        );

        graphics.drawString(
                font,
                "BEGIN >",
                startHereRect.x() + 10,
                startHereRect.y() + startHereRect.height() - 16,
                startHover ? COLOR_ACCENT : COLOR_TEXT_SECONDARY,
                false
        );

        int recentY = startHereRect.y() + startHereRect.height() + gap;

        int serverSettingsHeight = 62;
        int recentHeight =
                Math.max(
                        58,
                        contentBottom
                                - recentY
                                - gap
                                - serverSettingsHeight
                );

        Rect recentRect =
                new Rect(
                        leftX,
                        recentY,
                        leftWidth,
                        recentHeight
                );

        drawCard(graphics, recentRect, false);

        graphics.drawString(
                font,
                "RECENT",
                recentRect.x() + 10,
                recentRect.y() + 10,
                COLOR_ACCENT,
                false
        );

        if (recentEntries.isEmpty()) {
            graphics.drawString(
                    font,
                    "Nothing viewed yet.",
                    recentRect.x() + 10,
                    recentRect.y() + 30,
                    COLOR_TEXT_MUTED,
                    false
            );
        } else {
            int y = recentRect.y() + 29;
            int maxItems =
                    Math.max(
                            1,
                            (recentRect.height() - 40) / 18
                    );

            for (int i = 0; i < Math.min(maxItems, recentEntries.size()); i++) {
                RecentEntry entry = recentEntries.get(i);

                Rect hit =
                        new Rect(
                                recentRect.x() + 8,
                                y - 3,
                                recentRect.width() - 16,
                                16
                        );

                boolean hovered = hit.contains(mouseX, mouseY);

                graphics.drawString(
                        font,
                        trimToWidth(entry.title(), hit.width() - 12),
                        hit.x() + 2,
                        y,
                        hovered ? COLOR_ACCENT : COLOR_TEXT,
                        false
                );

                cardHitboxes.add(
                        new CardHitbox(
                                hit,
                                CardAction.RECENT,
                                entry.category(),
                                entry.subcategory(),
                                entry.pageIndex()
                        )
                );

                y += 18;
            }
        }

        Rect serverSettingsRect =
                new Rect(
                        leftX,
                        recentRect.y() + recentRect.height() + gap,
                        leftWidth,
                        serverSettingsHeight
                );

        boolean serverSettingsHover =
                serverSettingsRect.contains(mouseX, mouseY);

        drawCard(
                graphics,
                serverSettingsRect,
                serverSettingsHover
        );

        graphics.drawString(
                font,
                "SERVER SETTINGS",
                serverSettingsRect.x() + 10,
                serverSettingsRect.y() + 10,
                COLOR_ACCENT,
                false
        );

        String serverSettingsStatus =
                ClientServerSettingsData.hasReceivedSettings()
                        ? "Current configuration for this server"
                        : "Waiting for server configuration...";

        graphics.drawString(
                font,
                trimToWidth(
                        serverSettingsStatus,
                        serverSettingsRect.width() - 38
                ),
                serverSettingsRect.x() + 10,
                serverSettingsRect.y() + 29,
                COLOR_TEXT_SECONDARY,
                false
        );

        graphics.drawString(
                font,
                "VIEW >",
                serverSettingsRect.x()
                        + serverSettingsRect.width()
                        - font.width("VIEW >")
                        - 10,
                serverSettingsRect.y() + 44,
                serverSettingsHover
                        ? COLOR_ACCENT
                        : COLOR_TEXT_SECONDARY,
                false
        );

        cardHitboxes.add(
                new CardHitbox(
                        serverSettingsRect,
                        CardAction.CATEGORY,
                        SERVER_SETTINGS_CATEGORY,
                        null,
                        -1
                )
        );

        searchRect =
                new Rect(
                        rightX,
                        contentTop,
                        rightWidth,
                        34
                );

        drawCard(
                graphics,
                searchRect,
                searchRect.contains(mouseX, mouseY)
                        || searchBox.isFocused()
        );

        searchBox.setX(searchRect.x() + 8);
        searchBox.setY(searchRect.y() + 8);
        searchBox.setWidth(searchRect.width() - 30);
        searchBox.setHeight(18);

        searchBox.setVisible(true);

        graphics.drawString(
                font,
                "O",
                searchRect.x() + searchRect.width() - 18,
                searchRect.y() + 12,
                COLOR_TEXT_SECONDARY,
                false
        );

        boolean searching =
                searchBox != null
                        && !searchBox.getValue().isBlank();

        Rect categoriesRect = new Rect(
                rightX,
                searchRect.y() + searchRect.height() + gap,
                rightWidth,
                contentBottom - (searchRect.y() + searchRect.height() + gap)
        );

        if (searching) {
                renderSearchResults(
                        graphics,
                        mouseX,
                        mouseY,
                        categoriesRect
                );
        } else {
                renderHomeCategories(
                        graphics,
                        mouseX,
                        mouseY,
                        categoriesRect
                );
        }

    }

    private void renderHomeCategories(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                Rect categoriesRect
        ) {
        drawCard(
                graphics,
                categoriesRect,
                false
        );

        graphics.drawString(
                font,
                "CATEGORIES",
                categoriesRect.x() + 10,
                categoriesRect.y() + 10,
                COLOR_ACCENT,
                false
        );

        int itemY = categoriesRect.y() + 29;

        for (FloodGuideCategory category : getVisibleCategories()) {
                if (itemY + 14
                        > categoriesRect.y()
                        + categoriesRect.height()
                        - 25) {
                break;
                }

                Rect hit =
                        new Rect(
                                categoriesRect.x() + 8,
                                itemY - 3,
                                categoriesRect.width() - 16,
                                16
                        );

                boolean hovered =
                        hit.contains(mouseX, mouseY);

                graphics.drawString(
                        font,
                        trimToWidth(
                                category.displayName(),
                                hit.width() - 16
                        ),
                        hit.x() + 2,
                        itemY,
                        hovered
                                ? COLOR_ACCENT
                                : COLOR_TEXT,
                        false
                );

                graphics.drawString(
                        font,
                        ">",
                        hit.x() + hit.width() - 8,
                        itemY,
                        hovered
                                ? COLOR_ACCENT
                                : COLOR_TEXT_MUTED,
                        false
                );

                cardHitboxes.add(
                        new CardHitbox(
                                hit,
                                CardAction.CATEGORY,
                                category,
                                null,
                                -1
                        )
                );

                itemY += 18;
        }

        Rect viewAllRect =
                new Rect(
                        categoriesRect.x() + 8,
                        categoriesRect.y()
                                + categoriesRect.height()
                                - 21,
                        categoriesRect.width() - 16,
                        15
                );

        graphics.drawCenteredString(
                font,
                "VIEW ALL",
                viewAllRect.x()
                        + viewAllRect.width() / 2,
                viewAllRect.y() + 3,
                viewAllRect.contains(mouseX, mouseY)
                        ? COLOR_ACCENT
                        : COLOR_TEXT_SECONDARY
        );

        cardHitboxes.add(
                new CardHitbox(
                        viewAllRect,
                        CardAction.ALL_CATEGORIES,
                        null,
                        null,
                        -1
                )
        );
        }

        private void updateSearchResults() {
    searchResults.clear();

    if (searchBox == null) {
        return;
    }

    String query =
            searchBox.getValue()
                    .strip()
                    .toLowerCase();

    if (query.isEmpty()) {
        return;
    }

    for (FloodGuideCategory category
            : FloodGuideLoader.getCategories()
                .stream()
                .filter(FloodGuideVisibility::isVisible)
                .toList()
        ) {

        /*
         * Direct category articles.
         */
        List<FloodGuidePage> directPages =
                getVisiblePages(category);

        for (int i = 0; i < directPages.size(); i++) {
        FloodGuidePage page =
                directPages.get(i);

            int score =
                    calculateSearchScore(
                            query,
                            category,
                            null,
                            page
                    );

            if (score > 0) {
                searchResults.add(
                        new SearchResult(
                                category,
                                null,
                                i,
                                page,
                                score
                        )
                );
            }
        }

        /*
         * Subcategory articles.
         */
        for (FloodGuideSubcategory subcategory
                : getVisibleSubcategories(category)) {

        List<FloodGuidePage> pages =
                getVisiblePages(subcategory);

        for (int i = 0;
                i < pages.size();
                i++) {

                FloodGuidePage page =
                        pages.get(i);

                int score =
                        calculateSearchScore(
                                query,
                                category,
                                subcategory,
                                page
                        );

                if (score > 0) {
                    searchResults.add(
                            new SearchResult(
                                    category,
                                    subcategory,
                                    i,
                                    page,
                                    score
                            )
                    );
                }
            }
        }
    }

    searchResults.sort(
            Comparator.comparingInt(
                    SearchResult::score
            ).reversed()
                    .thenComparing(
                            result ->
                                    result.page()
                                            .title()
                    )
    );
}

        private int calculateSearchScore(
        String query,
        FloodGuideCategory category,
        FloodGuideSubcategory subcategory,
        FloodGuidePage page
) {
    String title =
            page.title().toLowerCase();

    String categoryName =
            category.displayName().toLowerCase();

    String subcategoryName =
            subcategory == null
                    ? ""
                    : subcategory.displayName()
                            .toLowerCase();

    String body =
            page.body() == null
                    ? ""
                    : page.body().toLowerCase();

    if (title.equals(query)) {
        return 1000;
    }

    if (title.startsWith(query)) {
        return 800;
    }

    if (title.contains(query)) {
        return 600;
    }

    if (subcategoryName.contains(query)) {
        return 400;
    }

    if (categoryName.contains(query)) {
        return 300;
    }

    if (body.contains(query)) {
        return 100;
    }

    return 0;
}

        private void renderSearchResults(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        Rect rect
) {
    drawCard(
            graphics,
            rect,
            false
    );

    graphics.drawString(
            font,
            "SEARCH RESULTS",
            rect.x() + 10,
            rect.y() + 10,
            COLOR_ACCENT,
            false
    );

    if (searchResults.isEmpty()) {
        graphics.drawString(
                font,
                "No results found.",
                rect.x() + 10,
                rect.y() + 31,
                COLOR_TEXT_MUTED,
                false
        );

        return;
    }

    int y = rect.y() + 29;
    int bottom =
            rect.y()
                    + rect.height()
                    - 8;

    for (int i = 0;
         i < searchResults.size();
         i++) {

        if (y + 28 > bottom) {
            break;
        }

        SearchResult result =
                searchResults.get(i);

        Rect hit =
                new Rect(
                        rect.x() + 8,
                        y - 3,
                        rect.width() - 16,
                        27
                );

        boolean hovered =
                hit.contains(mouseX, mouseY);

        graphics.drawString(
                font,
                trimToWidth(
                        result.page().title(),
                        hit.width() - 18
                ),
                hit.x() + 2,
                y,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_TEXT,
                false
        );

        String location =
                result.subcategory() == null
                        ? result.category()
                                .displayName()
                        : result.category()
                                .displayName()
                                + " > "
                                + result.subcategory()
                                .displayName();

        graphics.drawString(
                font,
                trimToWidth(
                        location,
                        hit.width() - 18
                ),
                hit.x() + 2,
                y + 11,
                COLOR_TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                ">",
                hit.x() + hit.width() - 8,
                y + 5,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_TEXT_MUTED,
                false
        );

        cardHitboxes.add(
                new CardHitbox(
                        hit,
                        CardAction.SEARCH_RESULT,
                        result.category(),
                        result.subcategory(),
                        result.pageIndex()
                )
        );

        y += 30;
    }
}

    private void renderCategories(GuiGraphics graphics, int mouseX, int mouseY) {
        renderSectionHeading(graphics, "CATEGORIES", "Browse every section of the guide.");

        List<FloodGuideCategory> categories = getVisibleCategories();

        int listTop = contentTop + 34;
        int availableHeight = contentBottom - listTop - 18;
        int columns = panelWidth >= 520 ? 2 : 1;
        int gap = 9;
        int cardHeight = 54;
        int rows = Math.max(1, availableHeight / (cardHeight + gap));
        int perPage = Math.max(1, rows * columns);
        int pageCount = pageCount(categories.size(), perPage);

        clampBrowserPage(pageCount);

        int start = browserPage * perPage;
        int end = Math.min(categories.size(), start + perPage);
        int cardWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;

        for (int i = start; i < end; i++) {
            int local = i - start;
            int column = local % columns;
            int row = local / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = listTop + row * (cardHeight + gap);

            FloodGuideCategory category = categories.get(i);
            Rect rect = new Rect(x, y, cardWidth, cardHeight);
            boolean hovered = rect.contains(mouseX, mouseY);

            drawCard(graphics, rect, hovered);
            renderPlaceholderIcon(
                    graphics,
                    rect.x() + 8,
                    rect.y() + 10,
                    34,
                    category.displayName().substring(0, 1),
                    hovered
            );

            graphics.drawString(font, category.displayName(), rect.x() + 51, rect.y() + 12, COLOR_TEXT, false);

            int count =
                getVisiblePages(category).size()
                        + getVisibleSubcategories(category).size();

            String subtitle =
                    count
                            + (count == 1
                            ? " topic"
                            : " topics");

            graphics.drawString(font, subtitle, rect.x() + 51, rect.y() + 29, COLOR_TEXT_MUTED, false);
            graphics.drawString(
                    font,
                    ">",
                    rect.x() + rect.width() - 15,
                    rect.y() + 22,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_MUTED,
                    false
            );

            cardHitboxes.add(new CardHitbox(rect, CardAction.CATEGORY, category, null, -1));
        }

        renderBrowserFooter(graphics, mouseX, mouseY, pageCount);
    }

    private void renderCategory(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedCategory == null) {
            goHome();
            return;
        }

        if (selectedCategory == SERVER_SETTINGS_CATEGORY) {
            renderServerSettingsCategory(graphics, mouseX, mouseY);
            return;
        }

        List<FloodGuidePage> directPages =
                getVisiblePages(selectedCategory);

        List<FloodGuideSubcategory> subcategories =
                getVisibleSubcategories(selectedCategory);

        int totalTopics =
                directPages.size()
                        + subcategories.size();

        renderSectionHeading(
                graphics,
                selectedCategory.displayName().toUpperCase(),
                totalTopics + (totalTopics == 1 ? " topic" : " topics")
        );

        if (totalTopics == 0) {
            renderEmptyMessage(graphics, "No topics have been added yet.");
            return;
        }

        /*
         * A category does NOT need a subcategory.
         *
         * If it contains direct article JSON files, those articles are shown
         * immediately on the category page.
         *
         * If it contains subcategory folders, those are shown as the existing
         * subcategory cards.
         *
         * Categories may also contain both.
         */
        if (!directPages.isEmpty() && subcategories.isEmpty()) {
            renderPageCards(
                    graphics,
                    mouseX,
                    mouseY,
                    directPages,
                    null
            );
            return;
        }

        if (directPages.isEmpty()) {
            renderSubcategoryCards(
                    graphics,
                    mouseX,
                    mouseY,
                    subcategories
            );
            return;
        }

        renderMixedCategoryCards(
                graphics,
                mouseX,
                mouseY,
                directPages,
                subcategories
        );
    }

    private void renderMixedCategoryCards(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            List<FloodGuidePage> directPages,
            List<FloodGuideSubcategory> subcategories
    ) {
        List<CategoryItem> items = new ArrayList<>();

        for (int i = 0; i < directPages.size(); i++) {
            FloodGuidePage page = directPages.get(i);
            items.add(CategoryItem.article(page.order(), page.title(), i));
        }

        for (FloodGuideSubcategory subcategory : subcategories) {
            items.add(CategoryItem.subcategory(
                    subcategory.order(),
                    subcategory.displayName(),
                    subcategory
            ));
        }

        items.sort(
                Comparator.comparingInt(CategoryItem::order)
                        .thenComparing(CategoryItem::title)
        );

        int listTop = contentTop + 34;
        int availableHeight = contentBottom - listTop - 18;
        int columns = panelWidth >= 520 ? 2 : 1;
        int gap = 9;
        int cardHeight = 52;
        int rows = Math.max(1, availableHeight / (cardHeight + gap));
        int perPage = Math.max(1, rows * columns);
        int pageCount = pageCount(items.size(), perPage);

        clampBrowserPage(pageCount);

        int start = browserPage * perPage;
        int end = Math.min(items.size(), start + perPage);
        int cardWidth =
                (contentRight - contentLeft - gap * (columns - 1))
                        / columns;

        for (int i = start; i < end; i++) {
            int local = i - start;
            int column = local % columns;
            int row = local / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = listTop + row * (cardHeight + gap);

            CategoryItem item = items.get(i);
            Rect rect = new Rect(x, y, cardWidth, cardHeight);
            boolean hovered = rect.contains(mouseX, mouseY);

            drawCard(graphics, rect, hovered);

            if (item.subcategory() != null) {
                FloodGuideSubcategory subcategory = item.subcategory();

                renderPlaceholderIcon(
                        graphics,
                        rect.x() + 8,
                        rect.y() + 9,
                        34,
                        subcategory.displayName().substring(0, 1),
                        hovered
                );

                graphics.drawString(
                        font,
                        trimToWidth(
                                subcategory.displayName(),
                                rect.width() - 72
                        ),
                        rect.x() + 51,
                        rect.y() + 8,
                        COLOR_TEXT,
                        false
                );

                String pageText =
                        subcategory.pageCount()
                                + (subcategory.pageCount() == 1
                                ? " page"
                                : " pages");

                graphics.drawString(
                        font,
                        pageText,
                        rect.x() + 51,
                        rect.y() + 22,
                        COLOR_TEXT_MUTED,
                        false
                );

                graphics.drawString(
                        font,
                        trimToWidth(
                                subcategory.description(),
                                rect.width() - 72
                        ),
                        rect.x() + 51,
                        rect.y() + 36,
                        COLOR_TEXT_SECONDARY,
                        false
                );

                cardHitboxes.add(
                        new CardHitbox(
                                rect,
                                CardAction.SUBCATEGORY,
                                selectedCategory,
                                subcategory,
                                -1
                        )
                );
            } else {
                graphics.drawString(
                        font,
                        trimToWidth(item.title(), rect.width() - 42),
                        rect.x() + 10,
                        rect.y() + 17,
                        COLOR_TEXT,
                        false
                );

                graphics.drawString(
                        font,
                        "ARTICLE",
                        rect.x() + 10,
                        rect.y() + 31,
                        COLOR_TEXT_MUTED,
                        false
                );

                cardHitboxes.add(
                        new CardHitbox(
                                rect,
                                CardAction.ARTICLE,
                                selectedCategory,
                                null,
                                item.pageIndex()
                        )
                );
            }

            graphics.drawString(
                    font,
                    ">",
                    rect.x() + rect.width() - 15,
                    rect.y() + 21,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_MUTED,
                    false
            );
        }

        renderBrowserFooter(graphics, mouseX, mouseY, pageCount);
    }

    private void renderSubcategoryCards(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            List<FloodGuideSubcategory> subcategories
    ) {
        int listTop = contentTop + 34;
        int availableHeight = contentBottom - listTop - 18;
        int columns = panelWidth >= 520 ? 2 : 1;
        int gap = 9;
        int cardHeight = 58;
        int rows = Math.max(1, availableHeight / (cardHeight + gap));
        int perPage = Math.max(1, rows * columns);
        int pageCount = pageCount(subcategories.size(), perPage);

        clampBrowserPage(pageCount);

        int start = browserPage * perPage;
        int end = Math.min(subcategories.size(), start + perPage);
        int cardWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;

        for (int i = start; i < end; i++) {
            int local = i - start;
            int column = local % columns;
            int row = local / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = listTop + row * (cardHeight + gap);

            FloodGuideSubcategory subcategory = subcategories.get(i);
            Rect rect = new Rect(x, y, cardWidth, cardHeight);
            boolean hovered = rect.contains(mouseX, mouseY);

            drawCard(graphics, rect, hovered);
            renderPlaceholderIcon(
                    graphics,
                    rect.x() + 8,
                    rect.y() + 12,
                    34,
                    subcategory.displayName().substring(0, 1),
                    hovered
            );

            graphics.drawString(
                    font,
                    trimToWidth(subcategory.displayName(), rect.width() - 72),
                    rect.x() + 51,
                    rect.y() + 10,
                    COLOR_TEXT,
                    false
            );

            String pageText = subcategory.pageCount() + (subcategory.pageCount() == 1 ? " page" : " pages");
            graphics.drawString(font, pageText, rect.x() + 51, rect.y() + 25, COLOR_TEXT_MUTED, false);

            graphics.drawString(
                    font,
                    trimToWidth(subcategory.description(), rect.width() - 72),
                    rect.x() + 51,
                    rect.y() + 40,
                    COLOR_TEXT_SECONDARY,
                    false
            );

            graphics.drawString(
                    font,
                    ">",
                    rect.x() + rect.width() - 15,
                    rect.y() + 24,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_MUTED,
                    false
            );

            cardHitboxes.add(new CardHitbox(
                    rect,
                    CardAction.SUBCATEGORY,
                    selectedCategory,
                    subcategory,
                    -1
            ));
        }

        renderBrowserFooter(graphics, mouseX, mouseY, pageCount);
    }

    private void renderSubcategory(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedCategory == null || selectedSubcategory == null) {
            goHome();
            return;
        }

        List<FloodGuidePage> pages =
                getVisiblePages(selectedSubcategory);

        renderSectionHeading(
                graphics,
                selectedSubcategory.displayName().toUpperCase(),
                selectedSubcategory.description()
        );

        if (pages == null || pages.isEmpty()) {
            renderEmptyMessage(graphics, "No pages have been added yet.");
            return;
        }

        renderPageCards(
                graphics,
                mouseX,
                mouseY,
                pages,
                selectedSubcategory
        );
    }

    private void renderPageCards(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            List<FloodGuidePage> pages,
            FloodGuideSubcategory ownerSubcategory
    ) {
        int listTop = contentTop + 38;
        int availableHeight = contentBottom - listTop - 18;
        int columns = panelWidth >= 520 ? 2 : 1;
        int gap = 9;
        int cardHeight = 44;
        int rows = Math.max(1, availableHeight / (cardHeight + gap));
        int perPage = Math.max(1, rows * columns);
        int pageCount = pageCount(pages.size(), perPage);

        clampBrowserPage(pageCount);

        int start = browserPage * perPage;
        int end = Math.min(pages.size(), start + perPage);
        int cardWidth =
                (contentRight - contentLeft - gap * (columns - 1))
                        / columns;

        for (int i = start; i < end; i++) {
            int local = i - start;
            int column = local % columns;
            int row = local / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = listTop + row * (cardHeight + gap);

            FloodGuidePage page = pages.get(i);
            Rect rect = new Rect(x, y, cardWidth, cardHeight);
            boolean hovered = rect.contains(mouseX, mouseY);

            drawCard(graphics, rect, hovered);

            int numberWidth = 28;

            graphics.fill(
                    rect.x() + 7,
                    rect.y() + 8,
                    rect.x() + 7 + numberWidth,
                    rect.y() + rect.height() - 8,
                    COLOR_CARD_DARK
            );

            drawBorder(
                    graphics,
                    rect.x() + 7,
                    rect.y() + 8,
                    numberWidth,
                    rect.height() - 16,
                    hovered ? COLOR_ACCENT : COLOR_BORDER
            );

            graphics.drawCenteredString(
                    font,
                    String.valueOf(i + 1),
                    rect.x() + 7 + numberWidth / 2,
                    rect.y() + 17,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_SECONDARY
            );

            graphics.drawString(
                    font,
                    trimToWidth(page.title(), rect.width() - 67),
                    rect.x() + 44,
                    rect.y() + 17,
                    COLOR_TEXT,
                    false
            );

            graphics.drawString(
                    font,
                    ">",
                    rect.x() + rect.width() - 15,
                    rect.y() + 17,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_MUTED,
                    false
            );

            cardHitboxes.add(
                    new CardHitbox(
                            rect,
                            CardAction.ARTICLE,
                            selectedCategory,
                            ownerSubcategory,
                            i
                    )
            );
        }

        renderBrowserFooter(graphics, mouseX, mouseY, pageCount);
    }

    private void renderArticle(GuiGraphics graphics, int mouseX, int mouseY) {
        FloodGuidePage page = getSelectedPage();

        if (page == null || selectedCategory == null) {
            goBack();
            return;
        }

        String title = page.title().toUpperCase();
        graphics.drawString(font, title, contentLeft, contentTop, COLOR_TEXT, false);
        graphics.fill(
                contentLeft,
                contentTop + 12,
                contentLeft + Math.min(font.width(title), 210),
                contentTop + 13,
                COLOR_ACCENT
        );

        int pageCount = getSelectedPageCount();
        String pageIndicator = (selectedPage + 1) + " / " + pageCount;

        graphics.drawString(
                font,
                pageIndicator,
                contentRight - font.width(pageIndicator),
                contentTop,
                COLOR_TEXT_MUTED,
                false
        );

        int bodyTop = contentTop + 27;
        int bodyBottom = contentBottom - 24;
        boolean showVisualPanel = panelWidth >= 530;
        int visualWidth = showVisualPanel
                ? Math.min(190, (contentRight - contentLeft) * 34 / 100)
                : 0;
        int gap = showVisualPanel ? 15 : 0;
        int textWidth = contentRight - contentLeft - visualWidth - gap;

        renderWrappedText(
                graphics,
                page.body(),
                contentLeft,
                bodyTop,
                textWidth,
                COLOR_TEXT_SECONDARY,
                Integer.MAX_VALUE,
                bodyBottom
        );

        if (showVisualPanel) {
            int visualX = contentRight - visualWidth;
            int visualHeight = Math.min(172, Math.max(90, bodyBottom - bodyTop));
            Rect visualRect = new Rect(visualX, bodyTop, visualWidth, visualHeight);

            graphics.fill(
                    visualRect.x(),
                    visualRect.y(),
                    visualRect.x() + visualRect.width(),
                    visualRect.y() + visualRect.height(),
                    COLOR_PANEL_INSET
            );
            drawBorder(
                    graphics,
                    visualRect.x(),
                    visualRect.y(),
                    visualRect.width(),
                    visualRect.height(),
                    COLOR_BORDER
            );

            graphics.drawCenteredString(
                    font,
                    "VISUAL PREVIEW",
                    visualRect.x() + visualRect.width() / 2,
                    visualRect.y() + visualRect.height() / 2 - 5,
                    COLOR_TEXT_MUTED
            );
        }

        renderArticleNavigation(graphics, mouseX, mouseY, pageCount);
    }

    private void renderServerSettingsCategory(GuiGraphics graphics, int mouseX, int mouseY) {
        renderSectionHeading(
                graphics,
                "SERVER SETTINGS",
                ClientServerSettingsData.hasReceivedSettings()
                        ? "Live settings synchronized from this server."
                        : "Waiting for live server configuration."
        );

        if (!ClientServerSettingsData.hasReceivedSettings()) {
            renderEmptyMessage(graphics, "Server settings have not been received yet.");
            return;
        }

        List<ServerSettingsPage> pages = ClientServerSettingsData.getPages();

        if (pages.isEmpty()) {
            renderEmptyMessage(graphics, "No server settings are available.");
            return;
        }

        int listTop = contentTop + 38;
        int availableHeight = contentBottom - listTop - 18;
        int columns = panelWidth >= 520 ? 2 : 1;
        int gap = 9;
        int cardHeight = 48;
        int rows = Math.max(1, availableHeight / (cardHeight + gap));
        int perPage = Math.max(1, rows * columns);
        int pageCount = pageCount(pages.size(), perPage);

        clampBrowserPage(pageCount);

        int start = browserPage * perPage;
        int end = Math.min(pages.size(), start + perPage);
        int cardWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;

        for (int i = start; i < end; i++) {
            int local = i - start;
            int column = local % columns;
            int row = local / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = listTop + row * (cardHeight + gap);

            ServerSettingsPage page = pages.get(i);
            Rect rect = new Rect(x, y, cardWidth, cardHeight);
            boolean hovered = rect.contains(mouseX, mouseY);

            drawCard(graphics, rect, hovered);

            graphics.drawString(
                    font,
                    trimToWidth(page.title(), rect.width() - 28),
                    rect.x() + 9,
                    rect.y() + 10,
                    COLOR_TEXT,
                    false
            );

            graphics.drawString(
                    font,
                    trimToWidth(page.subtitle(), rect.width() - 28),
                    rect.x() + 9,
                    rect.y() + 26,
                    COLOR_TEXT_MUTED,
                    false
            );

            graphics.drawString(
                    font,
                    ">",
                    rect.x() + rect.width() - 15,
                    rect.y() + 20,
                    hovered ? COLOR_ACCENT : COLOR_TEXT_MUTED,
                    false
            );

            cardHitboxes.add(new CardHitbox(
                    rect,
                    CardAction.SERVER_ARTICLE,
                    selectedCategory,
                    null,
                    i
            ));
        }

        renderBrowserFooter(graphics, mouseX, mouseY, pageCount);
    }

    private void renderServerSettingsArticle(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ServerSettingsPage> pages = ClientServerSettingsData.getPages();

        if (selectedServerSettingsPage < 0 || selectedServerSettingsPage >= pages.size()) {
            view = View.CATEGORY;
            return;
        }

        ServerSettingsPage page = pages.get(selectedServerSettingsPage);
        String title = page.title().toUpperCase();

        graphics.drawString(font, title, contentLeft, contentTop, COLOR_TEXT, false);
        graphics.fill(
                contentLeft,
                contentTop + 12,
                contentLeft + Math.min(font.width(title), 210),
                contentTop + 13,
                COLOR_ACCENT
        );

        String pageIndicator = (selectedServerSettingsPage + 1) + " / " + pages.size();
        graphics.drawString(
                font,
                pageIndicator,
                contentRight - font.width(pageIndicator),
                contentTop,
                COLOR_TEXT_MUTED,
                false
        );

        graphics.drawString(font, page.subtitle(), contentLeft, contentTop + 19, COLOR_TEXT_MUTED, false);

        int y = contentTop + 39;
        int bottom = contentBottom - 26;

        if (page.entries().isEmpty()) {
            int modified = ClientServerSettingsData.getModifiedCount();

            graphics.drawString(
                    font,
                    "Loaded settings: " + ClientServerSettingsData.getSettings().size(),
                    contentLeft,
                    y,
                    COLOR_TEXT_SECONDARY,
                    false
            );

            y += 18;

            graphics.drawString(
                    font,
                    "Modified from defaults: " + modified,
                    contentLeft,
                    y,
                    modified > 0 ? COLOR_MODIFIED : COLOR_TEXT_SECONDARY,
                    false
            );
        } else {
            for (ServerSettingEntry entry : page.entries()) {
                if (y >= bottom) {
                    break;
                }

                graphics.drawString(font, entry.name(), contentLeft, y, COLOR_TEXT, false);
                graphics.drawString(
                        font,
                        entry.value(),
                        contentRight - font.width(entry.value()),
                        y,
                        entry.isModified() ? COLOR_MODIFIED : COLOR_TEXT_SECONDARY,
                        false
                );

                y += 13;

                if (entry.isModified()) {
                    String defaultText = "Default: " + entry.defaultValue();
                    graphics.drawString(
                            font,
                            defaultText,
                            contentRight - font.width(defaultText),
                            y,
                            COLOR_TEXT_MUTED,
                            false
                    );
                    y += 11;
                }

                y = renderWrappedText(
                        graphics,
                        entry.description(),
                        contentLeft + 6,
                        y,
                        contentRight - contentLeft - 12,
                        COLOR_TEXT_MUTED,
                        2,
                        bottom
                );

                y += 8;
            }
        }

        renderServerSettingsNavigation(graphics, mouseX, mouseY, pages.size());
    }

    private void renderBrowserFooter(GuiGraphics graphics, int mouseX, int mouseY, int pageCount) {
        if (pageCount <= 1) {
            return;
        }

        int y = contentBottom - 11;
        String pageText = (browserPage + 1) + " / " + pageCount;

        graphics.drawCenteredString(
                font,
                pageText,
                panelLeft + panelWidth / 2,
                y,
                COLOR_TEXT_MUTED
        );

        Rect previous = new Rect(contentLeft - 2, y - 4, 22, 16);
        Rect next = new Rect(contentRight - 20, y - 4, 22, 16);

        graphics.drawString(
                font,
                "<",
                contentLeft,
                y,
                browserPage > 0
                        ? (previous.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT)
                        : COLOR_TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                ">",
                contentRight - 6,
                y,
                browserPage < pageCount - 1
                        ? (next.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT)
                        : COLOR_TEXT_MUTED,
                false
        );

        cardHitboxes.add(new CardHitbox(
                previous,
                CardAction.PREVIOUS_BROWSER_PAGE,
                null,
                null,
                pageCount
        ));

        cardHitboxes.add(new CardHitbox(
                next,
                CardAction.NEXT_BROWSER_PAGE,
                null,
                null,
                pageCount
        ));
    }

    private void renderArticleNavigation(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int pageCount
    ) {
        int y = contentBottom - 12;
        String previousText = "< PREVIOUS";
        String nextText = "NEXT >";

        Rect previousRect = new Rect(
                contentLeft,
                y - 4,
                font.width(previousText),
                16
        );

        int nextX = contentRight - font.width(nextText);
        Rect nextRect = new Rect(nextX, y - 4, font.width(nextText), 16);

        boolean hasPrevious = selectedPage > 0;
        boolean hasNext = selectedPage < pageCount - 1;

        graphics.drawString(
                font,
                previousText,
                contentLeft,
                y,
                hasPrevious
                        ? (previousRect.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT_SECONDARY)
                        : COLOR_TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                nextText,
                nextX,
                y,
                hasNext
                        ? (nextRect.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT_SECONDARY)
                        : COLOR_TEXT_MUTED,
                false
        );

        if (hasPrevious) {
            cardHitboxes.add(new CardHitbox(
                    previousRect,
                    CardAction.PREVIOUS_ARTICLE,
                    selectedCategory,
                    selectedSubcategory,
                    -1
            ));
        }

        if (hasNext) {
            cardHitboxes.add(new CardHitbox(
                    nextRect,
                    CardAction.NEXT_ARTICLE,
                    selectedCategory,
                    selectedSubcategory,
                    -1
            ));
        }
    }

    private void renderServerSettingsNavigation(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int pageCount
    ) {
        int y = contentBottom - 12;
        String previousText = "< PREVIOUS";
        String nextText = "NEXT >";

        Rect previousRect = new Rect(
                contentLeft,
                y - 4,
                font.width(previousText),
                16
        );

        int nextX = contentRight - font.width(nextText);
        Rect nextRect = new Rect(nextX, y - 4, font.width(nextText), 16);

        boolean hasPrevious = selectedServerSettingsPage > 0;
        boolean hasNext = selectedServerSettingsPage < pageCount - 1;

        graphics.drawString(
                font,
                previousText,
                contentLeft,
                y,
                hasPrevious
                        ? (previousRect.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT_SECONDARY)
                        : COLOR_TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                nextText,
                nextX,
                y,
                hasNext
                        ? (nextRect.contains(mouseX, mouseY) ? COLOR_ACCENT : COLOR_TEXT_SECONDARY)
                        : COLOR_TEXT_MUTED,
                false
        );

        if (hasPrevious) {
            cardHitboxes.add(new CardHitbox(
                    previousRect,
                    CardAction.PREVIOUS_SERVER_ARTICLE,
                    selectedCategory,
                    null,
                    -1
            ));
        }

        if (hasNext) {
            cardHitboxes.add(new CardHitbox(
                    nextRect,
                    CardAction.NEXT_SERVER_ARTICLE,
                    selectedCategory,
                    null,
                    -1
            ));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (backRect != null && backRect.contains(mouseX, mouseY)) {
            goBack();
            return true;
        }

        if (homeRect != null && homeRect.contains(mouseX, mouseY)) {
            goHome();
            return true;
        }

        if (view == View.HOME && startHereRect != null && startHereRect.contains(mouseX, mouseY)) {
            FloodGuideCategory gettingStarted =
                    FloodGuideLoader.getCategory("getting_started");

            if (gettingStarted != null) {
                openCategory(gettingStarted);
            }
            return true;
        }

        for (CardHitbox hitbox : new ArrayList<>(cardHitboxes)) {
            if (!hitbox.rect().contains(mouseX, mouseY)) {
                continue;
            }

            switch (hitbox.action()) {
                case ALL_CATEGORIES -> {
                    view = View.CATEGORIES;
                    browserPage = 0;
                    return true;
                }

                case CATEGORY -> {
                    openCategory(hitbox.category());
                    return true;
                }

                case SUBCATEGORY -> {
                    openSubcategory(hitbox.subcategory());
                    return true;
                }

                case ARTICLE -> {
                    selectedSubcategory = hitbox.subcategory();
                    openArticle(hitbox.index());
                    return true;
                }

                case SERVER_ARTICLE -> {
                    selectedServerSettingsPage = hitbox.index();
                    view = View.ARTICLE;
                    return true;
                }

                case RECENT -> {
                    openRecent(hitbox);
                    return true;
                }

                case PREVIOUS_BROWSER_PAGE -> {
                    if (browserPage > 0) {
                        browserPage--;
                    }
                    return true;
                }

                case NEXT_BROWSER_PAGE -> {
                    if (browserPage < hitbox.index() - 1) {
                        browserPage++;
                    }
                    return true;
                }

                case PREVIOUS_ARTICLE -> {
                    if (selectedPage > 0) {
                        selectedPage--;
                        recordRecent();
                    }
                    return true;
                }

                case NEXT_ARTICLE -> {
                    if (selectedPage < getSelectedPageCount() - 1) {
                        selectedPage++;
                        recordRecent();
                    }
                    return true;
                }

                case PREVIOUS_SERVER_ARTICLE -> {
                    if (selectedServerSettingsPage > 0) {
                        selectedServerSettingsPage--;
                    }
                    return true;
                }

                case NEXT_SERVER_ARTICLE -> {
                    int count = ClientServerSettingsData.getPages().size();
                    if (selectedServerSettingsPage < count - 1) {
                        selectedServerSettingsPage++;
                    }
                    return true;
                }

                case SEARCH_RESULT -> {
                        selectedCategory =
                                hitbox.category();

                        selectedSubcategory =
                                hitbox.subcategory();

                        openArticle(
                                hitbox.index()
                        );

                        return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
        public boolean keyPressed(
                int keyCode,
                int scanCode,
                int modifiers
        ) {
        if (keyCode == 256) {
                onClose();
                return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
        }

    private void openCategory(FloodGuideCategory category) {
        if (category == null) {
                return;
        }

        if (category != SERVER_SETTINGS_CATEGORY
                && !FloodGuideVisibility.isVisible(category)) {
                return;
        }

        selectedCategory = category;
        selectedSubcategory = null;
        selectedPage = 0;
        selectedServerSettingsPage = 0;
        browserPage = 0;
        view = View.CATEGORY;
        }

    private void openSubcategory(
                FloodGuideSubcategory subcategory
        ) {
        if (subcategory == null
                || !FloodGuideVisibility.isVisible(subcategory)) {
                return;
        }

        selectedSubcategory = subcategory;
        selectedPage = 0;
        browserPage = 0;
        view = View.SUBCATEGORY;
        }

    private void openArticle(int pageIndex) {
        List<FloodGuidePage> pages = getCurrentPageList();

        if (pages == null
                || pageIndex < 0
                || pageIndex >= pages.size()) {
            return;
        }

        selectedPage = pageIndex;
        view = View.ARTICLE;
        recordRecent();
    }

    private void openRecent(CardHitbox hitbox) {
        if (hitbox.category() == null) {
            return;
        }

        selectedCategory = hitbox.category();
        selectedSubcategory = hitbox.subcategory();
        selectedPage = hitbox.index();
        browserPage = 0;

        if (getSelectedPage() == null) {
            return;
        }

        view = View.ARTICLE;
        recordRecent();
    }

    private void goBack() {
        switch (view) {
            case HOME -> onClose();

            case CATEGORIES -> {
                view = View.HOME;
                browserPage = 0;
            }

            case CATEGORY -> {
                view = View.CATEGORIES;
                selectedSubcategory = null;
                selectedPage = 0;
                browserPage = 0;
            }

            case SUBCATEGORY -> {
                view = View.CATEGORY;
                selectedPage = 0;
                browserPage = 0;
            }

            case ARTICLE -> {
                if (selectedCategory == SERVER_SETTINGS_CATEGORY
                        || selectedSubcategory == null) {
                    view = View.CATEGORY;
                } else {
                    view = View.SUBCATEGORY;
                }
                browserPage = 0;
            }
        }
    }

    private record SearchResult(
        FloodGuideCategory category,
        FloodGuideSubcategory subcategory,
        int pageIndex,
        FloodGuidePage page,
        int score
) {
}

    private void goHome() {
        view = View.HOME;
        selectedCategory = null;
        selectedSubcategory = null;
        selectedPage = 0;
        selectedServerSettingsPage = 0;
        browserPage = 0;
    }

    private void recordRecent() {
        FloodGuidePage page = getSelectedPage();

        if (selectedCategory == null || page == null) {
            return;
        }

        String subcategoryId =
                selectedSubcategory == null
                        ? null
                        : selectedSubcategory.id();

        recentEntries.removeIf(entry -> {
            String entrySubcategoryId =
                    entry.subcategory() == null
                            ? null
                            : entry.subcategory().id();

            return entry.category().id().equals(selectedCategory.id())
                    && Objects.equals(
                            entrySubcategoryId,
                            subcategoryId
                    )
                    && entry.pageIndex() == selectedPage;
        });

        recentEntries.add(
                0,
                new RecentEntry(
                        selectedCategory,
                        selectedSubcategory,
                        selectedPage,
                        page.title()
                )
        );

        while (recentEntries.size() > 6) {
            recentEntries.remove(recentEntries.size() - 1);
        }
    }

    private List<FloodGuidePage> getCurrentPageList() {
        if (selectedSubcategory != null) {
                return getVisiblePages(selectedSubcategory);
        }

        if (selectedCategory != null
                && selectedCategory != SERVER_SETTINGS_CATEGORY) {
                return getVisiblePages(selectedCategory);
        }

        return null;
        }

    private int getSelectedPageCount() {
        List<FloodGuidePage> pages = getCurrentPageList();
        return pages == null ? 0 : pages.size();
    }

    private FloodGuidePage getSelectedPage() {
        List<FloodGuidePage> pages = getCurrentPageList();

        if (pages == null
                || selectedPage < 0
                || selectedPage >= pages.size()) {
            return null;
        }

        return pages.get(selectedPage);
    }

    private String getSelectedServerSettingsTitle() {
        List<ServerSettingsPage> pages = ClientServerSettingsData.getPages();

        if (selectedServerSettingsPage < 0 || selectedServerSettingsPage >= pages.size()) {
            return "Server Settings";
        }

        return pages.get(selectedServerSettingsPage).title();
    }

    private void renderSectionHeading(GuiGraphics graphics, String title, String subtitle) {
        graphics.drawString(font, title, contentLeft, contentTop, COLOR_TEXT, false);
        graphics.fill(
                contentLeft,
                contentTop + 12,
                contentLeft + Math.min(font.width(title), 180),
                contentTop + 13,
                COLOR_ACCENT
        );

        if (subtitle != null && !subtitle.isBlank()) {
            graphics.drawString(
                    font,
                    trimToWidth(subtitle, contentRight - contentLeft),
                    contentLeft,
                    contentTop + 19,
                    COLOR_TEXT_MUTED,
                    false
            );
        }
    }

    private void renderEmptyMessage(GuiGraphics graphics, String message) {
        graphics.drawCenteredString(
                font,
                message,
                panelLeft + panelWidth / 2,
                contentTop + 80,
                COLOR_TEXT_MUTED
        );
    }

    private void renderPlaceholderIcon(
            GuiGraphics graphics,
            int x,
            int y,
            int size,
            String label,
            boolean hovered
    ) {
        graphics.fill(x, y, x + size, y + size, COLOR_CARD_DARK);
        drawBorder(graphics, x, y, size, size, hovered ? COLOR_ACCENT : COLOR_BORDER);

        graphics.drawCenteredString(
                font,
                label,
                x + size / 2,
                y + size / 2 - 4,
                hovered ? COLOR_ACCENT : COLOR_TEXT_SECONDARY
        );
    }

    private void drawCard(GuiGraphics graphics, Rect rect, boolean hovered) {
        graphics.fill(
                rect.x(),
                rect.y(),
                rect.x() + rect.width(),
                rect.y() + rect.height(),
                hovered ? COLOR_CARD_HOVER : COLOR_CARD
        );

        drawBorder(
                graphics,
                rect.x(),
                rect.y(),
                rect.width(),
                rect.height(),
                hovered ? COLOR_ACCENT : COLOR_BORDER
        );
    }

    private void drawTextButton(GuiGraphics graphics, String text, Rect rect, boolean hovered) {
        graphics.fill(
                rect.x(),
                rect.y(),
                rect.x() + rect.width(),
                rect.y() + rect.height(),
                hovered ? COLOR_CARD_HOVER : COLOR_PANEL_INSET
        );

        drawBorder(
                graphics,
                rect.x(),
                rect.y(),
                rect.width(),
                rect.height(),
                hovered ? COLOR_ACCENT : COLOR_BORDER
        );

        graphics.drawCenteredString(
                font,
                text,
                rect.x() + rect.width() / 2,
                rect.y() + 5,
                hovered ? COLOR_ACCENT : COLOR_TEXT_SECONDARY
        );
    }

    private void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }

        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private int renderWrappedText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int maxWidth,
            int color,
            int maxLines,
            int bottom
    ) {
        if (text == null || text.isBlank() || maxWidth <= 0) {
            return y;
        }

        int lineY = y;
        int renderedLines = 0;
        String[] paragraphs = text.split("\\n", -1);

        for (String paragraph : paragraphs) {
            if (renderedLines >= maxLines || lineY + 9 > bottom) {
                break;
            }

            if (paragraph.isBlank()) {
                lineY += 7;
                continue;
            }

            List<FormattedCharSequence> lines =
                font.split(
                        Component.literal(paragraph.stripLeading()),
                        maxWidth
                );

            for (FormattedCharSequence line : lines) {
                if (renderedLines >= maxLines || lineY + 9 > bottom) {
                    return lineY;
                }

                graphics.drawString(font, line, x, lineY, color, false);
                lineY += 10;
                renderedLines++;
            }

            lineY += 3;
        }

        return lineY;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (text == null) {
            return "";
        }

        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int available = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, available) + ellipsis;
    }

    private int pageCount(int itemCount, int perPage) {
        if (perPage <= 0) {
            return 1;
        }

        return Math.max(1, (itemCount + perPage - 1) / perPage);
    }

    private void clampBrowserPage(int pageCount) {
        if (browserPage < 0) {
            browserPage = 0;
        }

        if (browserPage >= pageCount) {
            browserPage = Math.max(0, pageCount - 1);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum View {
        HOME,
        CATEGORIES,
        CATEGORY,
        SUBCATEGORY,
        ARTICLE
    }

    private enum CardAction {
        ALL_CATEGORIES,
        CATEGORY,
        SUBCATEGORY,
        ARTICLE,
        SERVER_ARTICLE,
        RECENT,
        PREVIOUS_BROWSER_PAGE,
        NEXT_BROWSER_PAGE,
        PREVIOUS_ARTICLE,
        NEXT_ARTICLE,
        PREVIOUS_SERVER_ARTICLE,
        NEXT_SERVER_ARTICLE,
        SEARCH_RESULT
    }

    private record Rect(int x, int y, int width, int height) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }

    private record CardHitbox(
            Rect rect,
            CardAction action,
            FloodGuideCategory category,
            FloodGuideSubcategory subcategory,
            int index
    ) {
    }

    private record CategoryItem(
            int order,
            String title,
            FloodGuideSubcategory subcategory,
            int pageIndex
    ) {
        private static CategoryItem article(
                int order,
                String title,
                int pageIndex
        ) {
            return new CategoryItem(
                    order,
                    title,
                    null,
                    pageIndex
            );
        }

        private static CategoryItem subcategory(
                int order,
                String title,
                FloodGuideSubcategory subcategory
        ) {
            return new CategoryItem(
                    order,
                    title,
                    subcategory,
                    -1
            );
        }
    }

    private record RecentEntry(
            FloodGuideCategory category,
            FloodGuideSubcategory subcategory,
            int pageIndex,
            String title
    ) {
    }
}
