package com.jushymaso222.theflood.client.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FloodGuideScreen extends Screen {

    private final Screen parent;

    private FloodGuideCategory selectedCategory =
            FloodGuideCategory.GETTING_STARTED;

    private int currentPage =
            0;

    /*
     * Main guide panel dimensions.
     */
    private static final int PANEL_WIDTH =
            320;

    private static final int PANEL_HEIGHT =
            300;

    /*
     * Category tab dimensions.
     */
    private static final int TAB_WIDTH =
            90;

    private static final int TAB_HEIGHT =
            20;

    /*
     * Text area.
     */
    private static final int CONTENT_PADDING =
            16;

    public FloodGuideScreen(
            Screen parent
    ) {
        super(
                Component.literal(
                        "The Flood Guide"
                )
        );

        this.parent =
                parent;
    }

    @Override
    protected void init() {
        super.init();

        int panelLeft =
                width / 2
                        - PANEL_WIDTH / 2;

        int panelTop =
                height / 2
                        - PANEL_HEIGHT / 2;

        /*
         * ---------------------------------------------------------
         * CATEGORY BUTTONS
         * ---------------------------------------------------------
         */

        FloodGuideCategory[] categories =
                FloodGuideCategory.values();

        for (int i = 0;
             i < categories.length;
             i++) {

            FloodGuideCategory category =
                    categories[i];

            int tabX =
                    panelLeft
                            - TAB_WIDTH
                            - 4;

            int tabY =
                    panelTop
                            + i
                            * (
                            TAB_HEIGHT + 2
                    );

            addRenderableWidget(
                    Button.builder(
                            Component.literal(
                                    category.getDisplayName()
                            ),
                            button -> {
                                selectedCategory =
                                        category;

                                currentPage =
                                        0;
                            }
                    )
                    .bounds(
                            tabX,
                            tabY,
                            TAB_WIDTH,
                            TAB_HEIGHT
                    )
                    .build()
            );
        }

        /*
         * ---------------------------------------------------------
         * PREVIOUS PAGE
         * ---------------------------------------------------------
         */

        addRenderableWidget(
                Button.builder(
                        Component.literal("<"),
                        button -> previousPage()
                )
                .bounds(
                        panelLeft + 12,
                        panelTop
                                + PANEL_HEIGHT
                                - 28,
                        20,
                        20
                )
                .build()
        );

        /*
         * ---------------------------------------------------------
         * NEXT PAGE
         * ---------------------------------------------------------
         */

        addRenderableWidget(
                Button.builder(
                        Component.literal(">"),
                        button -> nextPage()
                )
                .bounds(
                        panelLeft
                                + PANEL_WIDTH
                                - 32,
                        panelTop
                                + PANEL_HEIGHT
                                - 28,
                        20,
                        20
                )
                .build()
        );

        /*
         * ---------------------------------------------------------
         * DONE
         * ---------------------------------------------------------
         */

        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button ->
                                Minecraft.getInstance()
                                        .setScreen(
                                                parent
                                        )
                )
                .bounds(
                        panelLeft
                                + PANEL_WIDTH / 2
                                - 40,
                        panelTop
                                + PANEL_HEIGHT
                                + 8,
                        80,
                        20
                )
                .build()
        );
    }

    private void previousPage() {

        int pageCount =
                getCurrentCategoryPageCount();

        if (pageCount <= 0) {
            currentPage = 0;
            return;
        }

        currentPage =
                Math.floorMod(
                        currentPage - 1,
                        pageCount
                );
    }

    private void nextPage() {

        int pageCount =
                getCurrentCategoryPageCount();

        if (pageCount <= 0) {
            currentPage = 0;
            return;
        }

        currentPage =
                (currentPage + 1)
                        % pageCount;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Dark background behind the guide.
         */
        renderBackground(
                graphics
        );

        int panelLeft =
                width / 2
                        - PANEL_WIDTH / 2;

        int panelTop =
                height / 2
                        - PANEL_HEIGHT / 2;

        /*
         * Main guide panel.
         */
        graphics.fill(
                panelLeft,
                panelTop,
                panelLeft + PANEL_WIDTH,
                panelTop + PANEL_HEIGHT,
                0xEE202020
        );

        /*
         * Border.
         */
        graphics.fill(
                panelLeft,
                panelTop,
                panelLeft + PANEL_WIDTH,
                panelTop + 1,
                0xFFFFFFFF
        );

        graphics.fill(
                panelLeft,
                panelTop + PANEL_HEIGHT - 1,
                panelLeft + PANEL_WIDTH,
                panelTop + PANEL_HEIGHT,
                0xFFFFFFFF
        );

        graphics.fill(
                panelLeft,
                panelTop,
                panelLeft + 1,
                panelTop + PANEL_HEIGHT,
                0xFFFFFFFF
        );

        graphics.fill(
                panelLeft + PANEL_WIDTH - 1,
                panelTop,
                panelLeft + PANEL_WIDTH,
                panelTop + PANEL_HEIGHT,
                0xFFFFFFFF
        );

        /*
         * Guide heading.
         */
        graphics.drawCenteredString(
                font,
                "THE FLOOD GUIDE",
                panelLeft
                        + PANEL_WIDTH / 2,
                panelTop + 10,
                0xFFFFFFFF
        );

        /*
         * Current category.
         */
        graphics.drawCenteredString(
                font,
                selectedCategory.getDisplayName(),
                panelLeft
                        + PANEL_WIDTH / 2,
                panelTop + 26,
                0xFFFFAA55
        );

        if (
                selectedCategory
                        == FloodGuideCategory.SERVER_SETTINGS
        ) {
            renderServerSettings(
                    graphics,
                    panelLeft,
                    panelTop
            );

            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );

            return;
        }

        List<FloodGuidePage> pages =
                FloodGuideContent.getPages(
                        selectedCategory
                );

        if (!pages.isEmpty()) {

            if (currentPage >= pages.size()) {
                currentPage = 0;
            }

            FloodGuidePage page =
                    pages.get(
                            currentPage
                    );

            /*
             * Page title.
             */
            graphics.drawString(
                    font,
                    page.title(),
                    panelLeft
                            + CONTENT_PADDING,
                    panelTop + 46,
                    0xFFFFFFFF,
                    false
            );

            /*
             * Body text.
             */
            renderWrappedText(
                    graphics,
                    page.body(),
                    panelLeft
                            + CONTENT_PADDING,
                    panelTop + 62,
                    PANEL_WIDTH
                            - CONTENT_PADDING * 2
            );

            /*
             * Page counter.
             */
            String pageText =
                    "Page "
                            + (
                            currentPage + 1
                    )
                            + " / "
                            + pages.size();

            graphics.drawCenteredString(
                    font,
                    pageText,
                    panelLeft
                            + PANEL_WIDTH / 2,
                    panelTop
                            + PANEL_HEIGHT
                            - 22,
                    0xFFBBBBBB
            );

        } else {

            graphics.drawCenteredString(
                    font,
                    "No guide entries yet.",
                    panelLeft
                            + PANEL_WIDTH / 2,
                    panelTop + 70,
                    0xFFAAAAAA
            );
        }

        /*
         * Render buttons/tabs after our background.
         */
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void renderServerSettingsEntries(
            GuiGraphics graphics,
            ServerSettingsPage page,
            int panelLeft,
            int panelTop
    ) {

        int x =
                panelLeft
                        + CONTENT_PADDING;

        int y =
                panelTop + 78;

        int maxWidth =
                PANEL_WIDTH
                        - CONTENT_PADDING * 2;

        for (
                ServerSettingEntry entry :
                page.entries()
        ) {

            /*
            * Setting name.
            */
            graphics.drawString(
                    font,
                    entry.name(),
                    x,
                    y,
                    0xFFFFFFFF,
                    false
            );

            y += 11;

            /*
            * Actual server value.
            */
            int valueColor =
                    entry.isModified()
                            ? 0xFFFFAA55
                            : 0xFFDDDDDD;

            graphics.drawString(
                    font,
                    entry.value(),
                    x + 8,
                    y,
                    valueColor,
                    false
            );

            y += 11;

            /*
            * If modified, show the normal Flood default.
            */
            if (entry.isModified()) {

                graphics.drawString(
                        font,
                        "Default: "
                                + entry.defaultValue(),
                        x + 8,
                        y,
                        0xFF777777,
                        false
                );

                y += 11;
            }

            /*
            * Description.
            */
            List<net.minecraft.util.FormattedCharSequence> description =
                    font.split(
                            Component.literal(
                                    entry.description()
                            ),
                            maxWidth - 8
                    );

            for (
                    net.minecraft.util.FormattedCharSequence line :
                    description
            ) {

                graphics.drawString(
                        font,
                        line,
                        x + 8,
                        y,
                        0xFF999999,
                        false
                );

                y += 10;
            }

            /*
            * Space between settings.
            */
            y += 7;
        }
    }

    private void renderServerOverview(
            GuiGraphics graphics,
            int panelLeft,
            int panelTop
    ) {

        int modified =
                ClientServerSettingsData
                        .getModifiedCount();

        int total =
                ClientServerSettingsData
                        .getSettings()
                        .size();

        int x =
                panelLeft
                        + CONTENT_PADDING;

        int y =
                panelTop + 78;

        graphics.drawString(
                font,
                "This server is running:",
                x,
                y,
                0xFFDDDDDD,
                false
        );

        y += 16;

        graphics.drawString(
                font,
                total
                        + " gameplay settings",
                x,
                y,
                0xFFFFFFFF,
                false
        );

        y += 12;

        int modifiedColor =
                modified > 0
                        ? 0xFFFFAA55
                        : 0xFFDDDDDD;

        graphics.drawString(
                font,
                modified
                        + " modified from default",
                x,
                y,
                modifiedColor,
                false
        );

        y += 22;

        graphics.drawString(
                font,
                "Legend:",
                x,
                y,
                0xFFFFFFFF,
                false
        );

        y += 13;

        graphics.drawString(
                font,
                "Default setting",
                x,
                y,
                0xFFDDDDDD,
                false
        );

        y += 12;

        graphics.drawString(
                font,
                "Modified setting",
                x,
                y,
                0xFFFFAA55,
                false
        );

        y += 20;

        renderWrappedText(
                graphics,
                """
                Modified values are highlighted so you can quickly see how this server differs from The Flood's intended defaults.

                Use the arrow buttons to inspect exact spawning, horde, Blood Moon, and mob progression settings.
                """,
                x,
                y,
                PANEL_WIDTH
                        - CONTENT_PADDING * 2
        );
    }

    private void renderServerSettings(
            GuiGraphics graphics,
            int panelLeft,
            int panelTop
    ) {

        /*
        * Packet hasn't arrived yet.
        */
        if (
                !ClientServerSettingsData
                        .hasReceivedSettings()
        ) {

            graphics.drawCenteredString(
                    font,
                    "Waiting for server settings...",
                    panelLeft
                            + PANEL_WIDTH / 2,
                    panelTop + 70,
                    0xFFAAAAAA
            );

            return;
        }

        List<ServerSettingsPage> pages =
                ClientServerSettingsData
                        .getPages();

        if (pages.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    "No server settings received.",
                    panelLeft
                            + PANEL_WIDTH / 2,
                    panelTop + 70,
                    0xFFAAAAAA
            );

            return;
        }

        if (currentPage >= pages.size()) {
            currentPage = 0;
        }

        ServerSettingsPage page =
                pages.get(
                        currentPage
                );

        /*
        * Page heading.
        */
        graphics.drawCenteredString(
                font,
                page.title(),
                panelLeft
                        + PANEL_WIDTH / 2,
                panelTop + 46,
                0xFFFFFFFF
        );

        /*
        * Small section label.
        */
        graphics.drawCenteredString(
                font,
                page.subtitle(),
                panelLeft
                        + PANEL_WIDTH / 2,
                panelTop + 58,
                0xFF999999
        );

        if (currentPage == 0) {

            renderServerOverview(
                    graphics,
                    panelLeft,
                    panelTop
            );

        } else {

            renderServerSettingsEntries(
                    graphics,
                    page,
                    panelLeft,
                    panelTop
            );
        }

        /*
        * Dynamic page counter.
        */
        String pageText =
                "Page "
                        + (currentPage + 1)
                        + " / "
                        + pages.size();

        graphics.drawCenteredString(
                font,
                pageText,
                panelLeft
                        + PANEL_WIDTH / 2,
                panelTop
                        + PANEL_HEIGHT
                        - 22,
                0xFFBBBBBB
        );
    }

    private void renderWrappedText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int maxWidth
    ) {
        int lineY =
                y;

        String[] paragraphs =
                text.split(
                        "\\n"
                );

        for (String paragraph :
                paragraphs) {

            if (paragraph.isBlank()) {
                lineY += 9;
                continue;
            }

            List<net.minecraft.util.FormattedCharSequence> lines =
                    font.split(
                            Component.literal(
                                    paragraph
                            ),
                            maxWidth
                    );

            for (
                    net.minecraft.util.FormattedCharSequence line :
                    lines
            ) {
                graphics.drawString(
                        font,
                        line,
                        x,
                        lineY,
                        0xFFDDDDDD,
                        false
                );

                lineY += 10;
            }

            lineY += 3;
        }
    }

    private int getCurrentCategoryPageCount() {

        if (
                selectedCategory
                        == FloodGuideCategory.SERVER_SETTINGS
        ) {
            return ClientServerSettingsData
                    .getPages()
                    .size();
        }

        return FloodGuideContent
                .getPages(
                        selectedCategory
                )
                .size();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance()
                .setScreen(
                        parent
                );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}