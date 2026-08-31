package com.jushymaso222.theflood.progression.milestone.client;

import com.jushymaso222.theflood.progression.HeatTier;
import com.jushymaso222.theflood.progression.client.ClientHeatData;
import com.jushymaso222.theflood.progression.milestone.MilestoneDefinition;
import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;
import com.jushymaso222.theflood.progression.milestone.compat.MilestoneCompatDefinition;
import com.jushymaso222.theflood.progression.milestone.compat.MilestoneCompatRegistry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FloodMilestoneScreen extends Screen {

    /*
     * ============================================
     * LAYOUT
     * ============================================
     */

    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 340;

    private static final int HEADER_HEIGHT = 42;
    private static final int TAB_HEIGHT = 24;
    private static final int FOOTER_HEIGHT = 26;

    private static final int CONTENT_PADDING = 18;

    private static final int NODE_WIDTH = 116;
    private static final int NODE_HEIGHT = 44;

    private static final int NODE_GAP_X = 28;
    private static final int NODE_GAP_Y = 24;

    private static final int TAB_GAP = 3;

    private static final int TAB_SIDE_PADDING = 10;
    private static final int TAB_MIN_WIDTH = 82;
    private static final int TAB_ARROW_WIDTH = 22;

    private static final int TOOLTIP_BACKGROUND =
                0xFF101010;

        private static final int TOOLTIP_BORDER =
                0xFF666666;

        private static final int TOOLTIP_TITLE_COLOR =
                0xFFFFFFFF;

        private static final int TOOLTIP_TEXT_COLOR =
                0xFFAAAAAA;

        private static final int TOOLTIP_SECONDARY_TEXT_COLOR =
                0xFFBBBBBB;


    /*
     * ============================================
     * STATE
     * ============================================
     */

    private List<MilestoneCompatDefinition> categories =
            List.of();

    private String selectedCategoryId;

    private int scrollOffset = 0;

    private int maxScroll = 0;
    private int tabStartIndex = 0;


    public FloodMilestoneScreen() {
        super(
                Component.literal(
                        "Milestones"
                )
        );
    }


    /*
     * ============================================
     * INIT
     * ============================================
     */

    @Override
    protected void init() {

        categories =
                MilestoneCompatRegistry.available();

        if (
                selectedCategoryId == null
                        || findCategory(
                                selectedCategoryId
                        ) == null
        ) {

            if (!categories.isEmpty()) {
                selectedCategoryId =
                        categories.get(0).id();
            }
        }

        scrollOffset = 0;
    }


    /*
     * ============================================
     * RENDER
     * ============================================
     */

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        renderBackground(
                graphics
        );

        int panelWidth =
                Math.min(
                        PANEL_WIDTH,
                        width - 30
                );

        int panelHeight =
                Math.min(
                        PANEL_HEIGHT,
                        height - 30
                );

        int x =
                (width - panelWidth) / 2;

        int y =
                (height - panelHeight) / 2;

        int accentColor =
                HeatTier.getColor(
                        ClientHeatData.getEffectiveHeat()
                );


        /*
         * Main background.
         */

        graphics.fill(
                x,
                y,
                x + panelWidth,
                y + panelHeight,
                0xE0101010
        );

        drawBorder(
                graphics,
                x,
                y,
                panelWidth,
                panelHeight,
                0xFF777777
        );


        /*
         * Header.
         */

        graphics.drawCenteredString(
                font,
                "MILESTONES",
                x + panelWidth / 2,
                y + 12,
                0xFFFFFFFF
        );

        graphics.fill(
                x + 1,
                y + HEADER_HEIGHT - 1,
                x + panelWidth - 1,
                y + HEADER_HEIGHT,
                0xFF444444
        );


        /*
         * Category tabs.
         */

        renderTabs(
                graphics,
                x,
                y + HEADER_HEIGHT,
                panelWidth,
                mouseX,
                mouseY,
                accentColor
        );


        /*
         * Milestone content.
         */

        int contentTop =
                y
                        + HEADER_HEIGHT
                        + TAB_HEIGHT
                        + 1;

        int contentBottom =
                y
                        + panelHeight
                        - FOOTER_HEIGHT;

        renderMilestones(
                graphics,
                x,
                contentTop,
                panelWidth,
                contentBottom - contentTop,
                mouseX,
                mouseY,
                accentColor
        );


        /*
         * Footer.
         */

        renderFooter(
                graphics,
                x,
                y + panelHeight - FOOTER_HEIGHT,
                panelWidth,
                accentColor
        );


        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }


    /*
     * ============================================
     * TABS
     * ============================================
     */

    private void renderTabs(
            GuiGraphics graphics,
            int panelX,
            int tabY,
            int panelWidth,
            int mouseX,
            int mouseY,
            int accentColor
    ) {

        if (categories.isEmpty()) {
            return;
        }


        /*
        * Determine whether every category can fit
        * naturally without navigation arrows.
        */

        int naturalTotalWidth = 0;

        for (
                MilestoneCompatDefinition category :
                categories
        ) {

            naturalTotalWidth +=
                    Math.max(
                            TAB_MIN_WIDTH,
                            font.width(
                                    category.displayName()
                            )
                                    + TAB_SIDE_PADDING * 2
                    );
        }

        naturalTotalWidth +=
                TAB_GAP
                        * Math.max(
                                0,
                                categories.size() - 1
                        );


        int fullAvailableWidth =
                panelWidth - 12;


        boolean needsPaging =
                naturalTotalWidth
                        > fullAvailableWidth;


        /*
        * No paging required.
        */

        if (!needsPaging) {

            int currentX =
                    panelX + 6;


            for (
                    MilestoneCompatDefinition category :
                    categories
            ) {

                int tabWidth =
                        Math.max(
                                TAB_MIN_WIDTH,
                                font.width(
                                        category.displayName()
                                )
                                        + TAB_SIDE_PADDING * 2
                        );


                renderCategoryTab(
                        graphics,
                        category,
                        currentX,
                        tabY,
                        tabWidth,
                        mouseX,
                        mouseY,
                        accentColor
                );


                currentX +=
                        tabWidth
                                + TAB_GAP;
            }


            return;
        }


        /*
        * Paging required.
        *
        * Reserve space on both sides for arrows.
        */

        int leftArrowX =
                panelX + 6;

        int rightArrowX =
                panelX
                        + panelWidth
                        - 6
                        - TAB_ARROW_WIDTH;


        int tabsStartX =
                leftArrowX
                        + TAB_ARROW_WIDTH
                        + TAB_GAP;


        int tabsEndX =
                rightArrowX
                        - TAB_GAP;


        boolean canGoLeft =
                tabStartIndex > 0;


        /*
        * Clamp the starting index.
        */

        tabStartIndex =
                Math.max(
                        0,
                        Math.min(
                                tabStartIndex,
                                categories.size() - 1
                        )
                );


        /*
        * Left arrow.
        */

        renderTabArrow(
                graphics,
                leftArrowX,
                tabY,
                "<",
                canGoLeft,
                isInside(
                        mouseX,
                        mouseY,
                        leftArrowX,
                        tabY,
                        TAB_ARROW_WIDTH,
                        TAB_HEIGHT
                ),
                accentColor
        );


        /*
        * Determine and render the visible category
        * window.
        */

        int currentX =
                tabsStartX;

        int lastVisibleIndex =
                tabStartIndex - 1;


        for (
                int i = tabStartIndex;
                i < categories.size();
                i++
        ) {

            MilestoneCompatDefinition category =
                    categories.get(i);


            int desiredWidth =
                    Math.max(
                            TAB_MIN_WIDTH,
                            font.width(
                                    category.displayName()
                            )
                                    + TAB_SIDE_PADDING * 2
                    );


            int remainingWidth =
                    tabsEndX - currentX;


            if (remainingWidth < TAB_MIN_WIDTH) {
                break;
            }


            int tabWidth =
                    Math.min(
                            desiredWidth,
                            remainingWidth
                    );


            renderCategoryTab(
                    graphics,
                    category,
                    currentX,
                    tabY,
                    tabWidth,
                    mouseX,
                    mouseY,
                    accentColor
            );


            lastVisibleIndex = i;


            currentX +=
                    tabWidth
                            + TAB_GAP;
        }


        boolean canGoRight =
                lastVisibleIndex
                        < categories.size() - 1;


        /*
        * Right arrow.
        */

        renderTabArrow(
                graphics,
                rightArrowX,
                tabY,
                ">",
                canGoRight,
                isInside(
                        mouseX,
                        mouseY,
                        rightArrowX,
                        tabY,
                        TAB_ARROW_WIDTH,
                        TAB_HEIGHT
                ),
                accentColor
        );
    }

    private void renderCategoryTab(
            GuiGraphics graphics,
            MilestoneCompatDefinition category,
            int x,
            int y,
            int tabWidth,
            int mouseX,
            int mouseY,
            int accentColor
    ) {

        boolean selected =
                category.id()
                        .equals(
                                selectedCategoryId
                        );


        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        tabWidth,
                        TAB_HEIGHT
                );


        graphics.fill(
                x,
                y,
                x + tabWidth,
                y + TAB_HEIGHT,
                selected
                        ? 0xFF222222
                        : hovered
                                ? 0xCC1D1D1D
                                : 0xAA151515
        );


        if (selected) {

            graphics.fill(
                    x,
                    y,
                    x + tabWidth,
                    y + 2,
                    accentColor
            );
        }


        String label =
                fitText(
                        category.displayName()
                                .getString(),
                        tabWidth - 8
                );


        int textColor =
                selected
                        ? 0xFFFFFFFF
                        : hovered
                                ? 0xFFDDDDDD
                                : 0xFF999999;


        graphics.drawCenteredString(
                font,
                label,
                x + tabWidth / 2,
                y + 8,
                textColor
        );
    }


    private void renderTabArrow(
            GuiGraphics graphics,
            int x,
            int y,
            String arrow,
            boolean enabled,
            boolean hovered,
            int accentColor
    ) {

        int backgroundColor;

        if (!enabled) {

            backgroundColor =
                    0x55151515;

        } else if (hovered) {

            backgroundColor =
                    0xFF292929;

        } else {

            backgroundColor =
                    0xCC1A1A1A;
        }


        graphics.fill(
                x,
                y,
                x + TAB_ARROW_WIDTH,
                y + TAB_HEIGHT,
                backgroundColor
        );


        int borderColor =
                enabled && hovered
                        ? accentColor
                        : 0xFF444444;


        drawBorder(
                graphics,
                x,
                y,
                TAB_ARROW_WIDTH,
                TAB_HEIGHT,
                borderColor
        );


        graphics.drawCenteredString(
                font,
                arrow,
                x + TAB_ARROW_WIDTH / 2,
                y + 8,
                enabled
                        ? hovered
                                ? accentColor
                                : 0xFFBBBBBB
                        : 0xFF444444
        );
    }


    /*
     * ============================================
     * MILESTONES
     * ============================================
     */

    private void renderMilestones(
            GuiGraphics graphics,
            int panelX,
            int contentY,
            int panelWidth,
            int contentHeight,
            int mouseX,
            int mouseY,
            int accentColor
    ) {

        List<MilestoneDefinition> milestones =
                getSelectedMilestones();


        if (milestones.isEmpty()) {

            graphics.drawCenteredString(
                    font,
                    "No milestones available.",
                    panelX + panelWidth / 2,
                    contentY
                            + contentHeight / 2,
                    0xFF777777
            );

            maxScroll = 0;

            return;
        }


        /*
         * Determine how many nodes fit per row.
         */

        int usableWidth =
                panelWidth
                        - CONTENT_PADDING * 2;

        int columns =
                Math.max(
                        1,
                        (
                                usableWidth
                                        + NODE_GAP_X
                        )
                                / (
                                        NODE_WIDTH
                                                + NODE_GAP_X
                                )
                );


        int rows =
                (
                        milestones.size()
                                + columns
                                - 1
                )
                        / columns;


        int requiredHeight =
                rows * NODE_HEIGHT
                        + Math.max(
                                0,
                                rows - 1
                        )
                        * NODE_GAP_Y;


        maxScroll =
                Math.max(
                        0,
                        requiredHeight
                                - (
                                        contentHeight
                                                - CONTENT_PADDING * 2
                                )
                );


        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maxScroll
                        )
                );


        /*
         * Scissor prevents nodes from drawing
         * outside the content region.
         */

        graphics.enableScissor(
                panelX + 1,
                contentY,
                panelX + panelWidth - 1,
                contentY + contentHeight
        );


        /*
         * First calculate every node position.
         */

        List<NodePosition> positions =
                new ArrayList<>();


        for (
                int i = 0;
                i < milestones.size();
                i++
        ) {

            int row =
                    i / columns;

            int column =
                    i % columns;


            int rowCount =
                    Math.min(
                            columns,
                            milestones.size()
                                    - row * columns
                    );


            int rowWidth =
                    rowCount * NODE_WIDTH
                            + Math.max(
                                    0,
                                    rowCount - 1
                            )
                            * NODE_GAP_X;


            int rowStartX =
                    panelX
                            + panelWidth / 2
                            - rowWidth / 2;


            int nodeX =
                    rowStartX
                            + column
                            * (
                                    NODE_WIDTH
                                            + NODE_GAP_X
                            );


            int nodeY =
                    contentY
                            + CONTENT_PADDING
                            + row
                            * (
                                    NODE_HEIGHT
                                            + NODE_GAP_Y
                            )
                            - scrollOffset;


            positions.add(
                    new NodePosition(
                            nodeX,
                            nodeY
                    )
            );
        }


        /*
         * Connections first so they appear behind
         * milestone cards.
         */

        for (
                int i = 0;
                i < positions.size() - 1;
                i++
        ) {

            NodePosition from =
                    positions.get(i);

            NodePosition to =
                    positions.get(i + 1);

            boolean completed =
                    ClientMilestoneData.isCompleted(
                            milestones.get(i + 1).id()
                    );

            drawConnection(
                    graphics,
                    from,
                    to,
                    completed
                            ? accentColor
                            : 0xFF444444
            );
        }


        /*
         * Nodes.
         */

        MilestoneDefinition hoveredMilestone =
                null;

        NodePosition hoveredPosition =
                null;


        for (
                int i = 0;
                i < milestones.size();
                i++
        ) {

            MilestoneDefinition milestone =
                    milestones.get(i);

            NodePosition position =
                    positions.get(i);


            boolean completed =
                    ClientMilestoneData.isCompleted(
                            milestone.id()
                    );


            boolean hovered =
                    isInside(
                            mouseX,
                            mouseY,
                            position.x(),
                            position.y(),
                            NODE_WIDTH,
                            NODE_HEIGHT
                    );


            renderNode(
                    graphics,
                    milestone,
                    position.x(),
                    position.y(),
                    completed,
                    hovered,
                    accentColor
            );


            if (hovered) {

                hoveredMilestone =
                        milestone;

                hoveredPosition =
                        position;
            }
        }


        graphics.disableScissor();


        /*
         * Tooltip must render after scissor is
         * disabled.
         */

        if (
                hoveredMilestone != null
                        && hoveredPosition != null
        ) {

            renderMilestoneTooltip(
                    graphics,
                    hoveredMilestone,
                    mouseX,
                    mouseY,
                    accentColor
            );
        }
    }


    /*
     * ============================================
     * NODE
     * ============================================
     */

    private void renderNode(
            GuiGraphics graphics,
            MilestoneDefinition milestone,
            int x,
            int y,
            boolean completed,
            boolean hovered,
            int accentColor
    ) {

        int backgroundColor;

        if (completed) {

            backgroundColor =
                    hovered
                            ? 0xEE292929
                            : 0xDD202020;

        } else {

            backgroundColor =
                    hovered
                            ? 0xDD202020
                            : 0xBB151515;
        }


        graphics.fill(
                x,
                y,
                x + NODE_WIDTH,
                y + NODE_HEIGHT,
                backgroundColor
        );


        int borderColor;

        if (completed) {

            borderColor =
                    accentColor;

        } else if (hovered) {

            borderColor =
                    0xFF777777;

        } else {

            borderColor =
                    0xFF444444;
        }


        drawBorder(
                graphics,
                x,
                y,
                NODE_WIDTH,
                NODE_HEIGHT,
                borderColor
        );


        /*
         * Completion marker.
         */

        if (completed) {

            graphics.fill(
                    x + 1,
                    y + 1,
                    x + 4,
                    y + NODE_HEIGHT - 1,
                    accentColor
            );
        }


        /*
         * Title.
         */

        String title =
                fitText(
                        milestone.title()
                                .getString(),
                        NODE_WIDTH - 14
                );


        graphics.drawCenteredString(
                font,
                title,
                x + NODE_WIDTH / 2,
                y + 8,
                completed
                        ? 0xFFFFFFFF
                        : 0xFF999999
        );


        /*
         * Progression value.
         */

        String progression =
                milestone.progressionValue()
                        + "%";


        graphics.drawCenteredString(
                font,
                progression,
                x + NODE_WIDTH / 2,
                y + 25,
                completed
                        ? accentColor
                        : 0xFF666666
        );
    }


    /*
     * ============================================
     * CONNECTIONS
     * ============================================
     */

    private void drawConnection(
            GuiGraphics graphics,
            NodePosition from,
            NodePosition to,
            int color
    ) {

        int startX =
                from.x()
                        + NODE_WIDTH / 2;

        int startY =
                from.y()
                        + NODE_HEIGHT;

        int endX =
                to.x()
                        + NODE_WIDTH / 2;

        int endY =
                to.y();


        /*
         * Same row.
         */

        if (
                from.y() == to.y()
        ) {

            int horizontalStart =
                    from.x()
                            + NODE_WIDTH;

            int horizontalEnd =
                    to.x();

            if (horizontalEnd > horizontalStart) {

                graphics.fill(
                        horizontalStart,
                        from.y()
                                + NODE_HEIGHT / 2,
                        horizontalEnd,
                        from.y()
                                + NODE_HEIGHT / 2
                                + 1,
                        color
                );
            }

            return;
        }


        /*
         * Row transition.
         *
         * Draw an elbow connector.
         */

        int middleY =
                startY
                        + (
                                endY - startY
                        )
                        / 2;


        graphics.fill(
                startX,
                startY,
                startX + 1,
                middleY,
                color
        );


        if (endX >= startX) {

            graphics.fill(
                    startX,
                    middleY,
                    endX + 1,
                    middleY + 1,
                    color
            );

        } else {

            graphics.fill(
                    endX,
                    middleY,
                    startX + 1,
                    middleY + 1,
                    color
            );
        }


        graphics.fill(
                endX,
                middleY,
                endX + 1,
                endY,
                color
        );
    }


    /*
     * ============================================
     * TOOLTIP
     * ============================================
     */

    private void renderMilestoneTooltip(
        GuiGraphics graphics,
        MilestoneDefinition milestone,
        int mouseX,
        int mouseY,
        int accentColor
) {

    int tooltipWidth = 190;

    List<FormattedCharSequence> description =
            font.split(
                    milestone.description(),
                    tooltipWidth - 16
            );

    int tooltipHeight =
            42
                    + description.size()
                    * font.lineHeight;

    int tooltipX =
            mouseX + 12;

    int tooltipY =
            mouseY + 12;

    if (
            tooltipX
                    + tooltipWidth
                    > width - 4
    ) {
        tooltipX =
                mouseX
                        - tooltipWidth
                        - 12;
    }

    if (
            tooltipY
                    + tooltipHeight
                    > height - 4
    ) {
        tooltipY =
                height
                        - tooltipHeight
                        - 4;
    }


    /*
     * Render the tooltip above every milestone
     * node, connector, and label.
     */
    graphics.pose().pushPose();

    graphics.pose().translate(
            0.0F,
            0.0F,
            400.0F
    );


    graphics.fill(
            tooltipX,
            tooltipY,
            tooltipX + tooltipWidth,
            tooltipY + tooltipHeight,
            TOOLTIP_BACKGROUND
    );

    drawBorder(
            graphics,
            tooltipX,
            tooltipY,
            tooltipWidth,
            tooltipHeight,
            TOOLTIP_BORDER
    );

    graphics.drawString(
            font,
            milestone.title(),
            tooltipX + 8,
            tooltipY + 7,
            TOOLTIP_TITLE_COLOR,
            true
    );

    int descriptionY =
            tooltipY + 20;

    for (
            int i = 0;
            i < description.size();
            i++
    ) {
        graphics.drawString(
                font,
                description.get(i),
                tooltipX + 8,
                descriptionY
                        + i * font.lineHeight,
                TOOLTIP_TEXT_COLOR,
                false
        );
    }

    int rewardY =
            descriptionY
                    + description.size()
                    * font.lineHeight
                    + 4;

    String progression =
            "Progression: "
                    + milestone.progressionValue()
                    + "%";

    graphics.drawString(
            font,
            progression,
            tooltipX + 8,
            rewardY,
            TOOLTIP_SECONDARY_TEXT_COLOR,
            false
    );

    String reward =
            "+"
                    + Math.round(
                            milestone.floodXpReward()
                                    * 100.0D
                    )
                    + "% Flood XP";

    int rewardWidth =
            font.width(
                    reward
            );

    graphics.drawString(
            font,
            reward,
            tooltipX
                    + tooltipWidth
                    - rewardWidth
                    - 8,
            rewardY,
            accentColor,
            false
    );


    graphics.pose().popPose();
}


    /*
     * ============================================
     * FOOTER
     * ============================================
     */

    private void renderFooter(
            GuiGraphics graphics,
            int x,
            int y,
            int panelWidth,
            int accentColor
    ) {

        graphics.fill(
                x + 1,
                y,
                x + panelWidth - 1,
                y + 1,
                0xFF444444
        );


        List<MilestoneDefinition> milestones =
                getSelectedMilestones();


        long completed =
                milestones.stream()
                        .filter(
                                milestone ->
                                        ClientMilestoneData
                                                .isCompleted(
                                                        milestone.id()
                                                )
                        )
                        .count();


        String completionText =
                completed
                        + " / "
                        + milestones.size()
                        + " COMPLETED";


        graphics.drawString(
                font,
                completionText,
                x + 10,
                y + 9,
                accentColor,
                false
        );


        if (maxScroll > 0) {

            graphics.drawString(
                    font,
                    "SCROLL TO VIEW",
                    x
                            + panelWidth
                            - font.width(
                                    "SCROLL TO VIEW"
                            )
                            - 10,
                    y + 9,
                    0xFF777777,
                    false
            );
        }
    }


    /*
     * ============================================
     * INPUT
     * ============================================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (
                button != 0
                        || categories.isEmpty()
        ) {

            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }


        int panelWidth =
                Math.min(
                        PANEL_WIDTH,
                        width - 30
                );


        int panelHeight =
                Math.min(
                        PANEL_HEIGHT,
                        height - 30
                );


        int panelX =
                (width - panelWidth) / 2;


        int panelY =
                (height - panelHeight) / 2;


        int tabY =
                panelY + HEADER_HEIGHT;


        /*
        * Determine whether paging is needed.
        */

        int naturalTotalWidth = 0;


        for (
                MilestoneCompatDefinition category :
                categories
        ) {

            naturalTotalWidth +=
                    Math.max(
                            TAB_MIN_WIDTH,
                            font.width(
                                    category.displayName()
                            )
                                    + TAB_SIDE_PADDING * 2
                    );
        }


        naturalTotalWidth +=
                TAB_GAP
                        * Math.max(
                                0,
                                categories.size() - 1
                        );


        int fullAvailableWidth =
                panelWidth - 12;


        boolean needsPaging =
                naturalTotalWidth
                        > fullAvailableWidth;


        /*
        * Everything fits.
        */

        if (!needsPaging) {

            int currentX =
                    panelX + 6;


            for (
                    MilestoneCompatDefinition category :
                    categories
            ) {

                int tabWidth =
                        Math.max(
                                TAB_MIN_WIDTH,
                                font.width(
                                        category.displayName()
                                )
                                        + TAB_SIDE_PADDING * 2
                        );


                if (
                        isInside(
                                mouseX,
                                mouseY,
                                currentX,
                                tabY,
                                tabWidth,
                                TAB_HEIGHT
                        )
                ) {

                    selectCategory(
                            category
                    );

                    return true;
                }


                currentX +=
                        tabWidth
                                + TAB_GAP;
            }


            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }


        /*
        * Paged tabs.
        */

        int leftArrowX =
                panelX + 6;


        int rightArrowX =
                panelX
                        + panelWidth
                        - 6
                        - TAB_ARROW_WIDTH;


        int tabsStartX =
                leftArrowX
                        + TAB_ARROW_WIDTH
                        + TAB_GAP;


        int tabsEndX =
                rightArrowX
                        - TAB_GAP;


        /*
        * Left arrow.
        */

        if (
                tabStartIndex > 0
                        && isInside(
                                mouseX,
                                mouseY,
                                leftArrowX,
                                tabY,
                                TAB_ARROW_WIDTH,
                                TAB_HEIGHT
                        )
        ) {

            tabStartIndex--;

            return true;
        }


        /*
        * Work out visible tabs.
        */

        int currentX =
                tabsStartX;


        int lastVisibleIndex =
                tabStartIndex - 1;


        for (
                int i = tabStartIndex;
                i < categories.size();
                i++
        ) {

            MilestoneCompatDefinition category =
                    categories.get(i);


            int desiredWidth =
                    Math.max(
                            TAB_MIN_WIDTH,
                            font.width(
                                    category.displayName()
                            )
                                    + TAB_SIDE_PADDING * 2
                    );


            int remainingWidth =
                    tabsEndX - currentX;


            if (remainingWidth < TAB_MIN_WIDTH) {
                break;
            }


            int tabWidth =
                    Math.min(
                            desiredWidth,
                            remainingWidth
                    );


            if (
                    isInside(
                            mouseX,
                            mouseY,
                            currentX,
                            tabY,
                            tabWidth,
                            TAB_HEIGHT
                    )
            ) {

                selectCategory(
                        category
                );

                return true;
            }


            lastVisibleIndex = i;


            currentX +=
                    tabWidth
                            + TAB_GAP;
        }


        /*
        * Right arrow.
        */

        if (
                lastVisibleIndex
                        < categories.size() - 1
                        && isInside(
                                mouseX,
                                mouseY,
                                rightArrowX,
                                tabY,
                                TAB_ARROW_WIDTH,
                                TAB_HEIGHT
                        )
        ) {

            tabStartIndex++;

            return true;
        }


        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }


    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        if (maxScroll <= 0) {

            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    delta
            );
        }


        scrollOffset -=
                (int) Math.round(
                        delta * 24.0D
                );


        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maxScroll
                        )
                );


        return true;
    }


    /*
     * ============================================
     * DATA
     * ============================================
     */

    private List<MilestoneDefinition> getSelectedMilestones() {

        if (selectedCategoryId == null) {
            return List.of();
        }


        return MilestoneRegistry.all()
                .stream()
                .filter(
                        milestone ->
                                selectedCategoryId.equals(
                                        milestone.categoryId()
                                )
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        MilestoneDefinition::progressionValue
                                )
                                .thenComparing(
                                        milestone ->
                                                milestone.id()
                                                        .toString()
                                )
                )
                .toList();
    }

    private void selectCategory(
            MilestoneCompatDefinition category
    ) {

        if (category == null) {
            return;
        }


        selectedCategoryId =
                category.id();


        scrollOffset = 0;
    }


    private MilestoneCompatDefinition findCategory(
            String id
    ) {

        if (id == null) {
            return null;
        }


        for (
                MilestoneCompatDefinition category :
                categories
        ) {

            if (
                    id.equals(
                            category.id()
                    )
            ) {

                return category;
            }
        }


        return null;
    }


    /*
     * ============================================
     * HELPERS
     * ============================================
     */

    private String fitText(
            String text,
            int maxWidth
    ) {

        if (
                text == null
                        || text.isEmpty()
        ) {

            return "";
        }


        if (
                font.width(
                        text
                )
                        <= maxWidth
        ) {

            return text;
        }


        String ellipsis =
                "...";

        int ellipsisWidth =
                font.width(
                        ellipsis
                );


        StringBuilder result =
                new StringBuilder();


        for (
                int i = 0;
                i < text.length();
                i++
        ) {

            String candidate =
                    result.toString()
                            + text.charAt(i);


            if (
                    font.width(
                            candidate
                    )
                            + ellipsisWidth
                            > maxWidth
            ) {

                break;
            }


            result.append(
                    text.charAt(i)
            );
        }


        return result
                + ellipsis;
    }


    private static boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }


    private static void drawBorder(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                color
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                color
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }


    private record NodePosition(
            int x,
            int y
    ) {
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}