package com.jushymaso222.theflood.hud;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.jushymaso222.theflood.team.client.TeamHudOverlay;
import net.minecraft.resources.ResourceLocation;
import com.jushymaso222.theflood.progression.client.HeatHudOverlay;

public class FloodHudLayoutScreen extends Screen {

    private final Screen parent;

    private static final int HEAT_SIZE = 32;

    private static final ResourceLocation FLOOD_HUD_PREVIEW =
                new ResourceLocation(
                        TheFlood.MOD_ID,
                        "textures/gui/progression/green_floodxp_empty.png"
                );

        private static final int FLOOD_HUD_TEXTURE_WIDTH = 629;
        private static final int FLOOD_HUD_TEXTURE_HEIGHT = 133;

        private static final float REFERENCE_GUI_WIDTH =
                2560.0F / 3.0F;

        private int getHudScaleWidth(
                int baseWidth
        ) {
        return Math.round(
                baseWidth
                        * (
                        width
                                / REFERENCE_GUI_WIDTH
                )
        );
        }

        private int getHeatPreviewWidth() {
        return HeatHudOverlay.getHudWidth(
                width
        );
        }

        private int getHeatPreviewHeight() {
        return HeatHudOverlay.getHudHeight(
                getHeatPreviewWidth()
        );
        }

        private int getHeatReservedWidth() {
        return HeatHudOverlay.getHudReservedWidth(
                minecraft,
                width
        );
        }

        private int getTeamPreviewWidth() {
        return TeamHudOverlay
                .getScaledCardWidth(
                        width
                );
        }

        private int getTeamPreviewHeight() {
        return TeamHudOverlay
                .getScaledCardHeight(
                        width
                ) * 2
                + TeamHudOverlay
                .getScaledCardSpacing(
                        width
                );
        }

    private DragTarget dragging = DragTarget.NONE;

    private HudAnchor heatAnchor;
        private int heatXOffset;
        private int heatYOffset;

        private HudAnchor teamAnchor;
        private int teamXOffset;
        private int teamYOffset;

        private static final int TEAM_PREVIEW_WIDTH =
                110;

        private static final int TEAM_PREVIEW_HEIGHT =
                60;

    public FloodHudLayoutScreen(Screen parent) {
        super(Component.literal("HUD Layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        heatAnchor =
                TheFloodClientConfig
                        .HEAT_HUD_ANCHOR
                        .get();

        heatXOffset =
                TheFloodClientConfig
                        .HEAT_HUD_ANCHOR_X_OFFSET
                        .get();

        heatYOffset =
                TheFloodClientConfig
                        .HEAT_HUD_ANCHOR_Y_OFFSET
                        .get();


        teamAnchor =
                TheFloodClientConfig
                        .TEAM_HUD_ANCHOR
                        .get();

        teamXOffset =
                TheFloodClientConfig
                        .TEAM_HUD_ANCHOR_X_OFFSET
                        .get();

        teamYOffset =
                TheFloodClientConfig
                        .TEAM_HUD_ANCHOR_Y_OFFSET
                        .get();

        addRenderableWidget(
                Button.builder(
                        Component.literal("Reset"),
                        button -> resetPositions()
                )
                .bounds(
                        width / 2 - 105,
                        height - 28,
                        100,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save & Done"),
                        button -> saveAndClose()
                )
                .bounds(
                        width / 2 + 5,
                        height - 28,
                        100,
                        20
                )
                .build()
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        graphics.drawCenteredString(
                font,
                "HUD LAYOUT",
                width / 2,
                12,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                font,
                "Drag the Flood HUD elements wherever you want them.",
                width / 2,
                27,
                0xFFAAAAAA
        );
        renderHudObstacles(graphics);

        renderHeatPreview(graphics);
        renderTeamPreview(
                graphics
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void renderHudObstacles(
        GuiGraphics graphics
) {
    /*
     * =================================================
     * VANILLA HUD
     * =================================================
     *
     * Simple representative region for:
     *
     * health
     * armor
     * hunger
     * air
     * XP
     * hotbar
     */

    int hudWidth =
            Math.min(
                    200,
                    width - 20
            );

    int hudHeight =
            46;

    int hudX =
            width / 2
                    - hudWidth / 2;

    int hudY =
            height
                    - hudHeight
                    - 5;

    drawObstacle(
            graphics,
            hudX,
            hudY,
            hudWidth,
            hudHeight,
            "VANILLA HUD"
    );


    /*
     * =================================================
     * MINIMAP
     * =================================================
     *
     * Generic top-right minimap reservation.
     *
     * We don't care which minimap mod is installed.
     * The player can use this as a placement guide
     * or simply ignore it.
     */

    int minimapSize =
            Math.min(
                    110,
                    Math.max(
                            70,
                            width / 7
                    )
            );

    int minimapX =
                8;

        int minimapY =
                8;

    drawObstacle(
            graphics,
            minimapX,
            minimapY,
            minimapSize,
            minimapSize,
            "MINIMAP"
    );
}

        private void drawObstacle(
        GuiGraphics graphics,
        int x,
        int y,
        int obstacleWidth,
        int obstacleHeight,
        String label
) {
    int background =
            0x40202020;

    int border =
            0x80666666;

    int textColor =
            0xFF888888;

    graphics.fill(
            x,
            y,
            x + obstacleWidth,
            y + obstacleHeight,
            background
    );

    graphics.fill(
            x,
            y,
            x + obstacleWidth,
            y + 1,
            border
    );

    graphics.fill(
            x,
            y + obstacleHeight - 1,
            x + obstacleWidth,
            y + obstacleHeight,
            border
    );

    graphics.fill(
            x,
            y,
            x + 1,
            y + obstacleHeight,
            border
    );

    graphics.fill(
            x + obstacleWidth - 1,
            y,
            x + obstacleWidth,
            y + obstacleHeight,
            border
    );

    int labelWidth =
            font.width(
                    label
            );

    graphics.drawString(
            font,
            label,
            x
                    + obstacleWidth / 2
                    - labelWidth / 2,
            y
                    + obstacleHeight / 2
                    - font.lineHeight / 2,
            textColor,
            false
    );
}

    private void renderTeamPreview(
                GuiGraphics graphics
        ) {
        int previewWidth =
                getTeamPreviewWidth();

        int previewHeight =
                getTeamPreviewHeight();

        FloodHudPositioning.Position position =
                FloodHudPositioning.resolveClamped(
                        teamAnchor,
                        width,
                        height,
                        previewWidth,
                        previewHeight,
                        teamXOffset,
                        teamYOffset
                );

        int x =
                position.x();

        int y =
                position.y();

        float scale =
                TeamHudOverlay.getHudScale(
                        width
                );

        int cardHeight =
                TeamHudOverlay
                        .getScaledCardHeight(
                                width
                        );

        int cardSpacing =
                TeamHudOverlay
                        .getScaledCardSpacing(
                                width
                        );

        renderScaledFakeTeammate(
                graphics,
                x,
                y,
                scale,
                "Bob",
                0xFF64B5F6,
                0.75F,
                "32m"
        );

        renderScaledFakeTeammate(
                graphics,
                x,
                y
                        + cardHeight
                        + cardSpacing,
                scale,
                "Alex",
                0xFFE57373,
                0.45F,
                "57m"
        );
        }

        private HudAnchor getClosestAnchor(
        double x,
        double y
) {
    boolean left =
            x < width / 3.0;

    boolean right =
            x > width * 2.0 / 3.0;

    boolean top =
            y < height / 3.0;

    boolean bottom =
            y > height * 2.0 / 3.0;

    if (top) {
        if (left) {
            return HudAnchor.TOP_LEFT;
        }

        if (right) {
            return HudAnchor.TOP_RIGHT;
        }

        return HudAnchor.TOP_CENTER;
    }

    if (bottom) {
        if (left) {
            return HudAnchor.BOTTOM_LEFT;
        }

        if (right) {
            return HudAnchor.BOTTOM_RIGHT;
        }

        return HudAnchor.BOTTOM_CENTER;
    }

    if (left) {
        return HudAnchor.CENTER_LEFT;
    }

    if (right) {
        return HudAnchor.CENTER_RIGHT;
    }

    return HudAnchor.CENTER;
}

        private void renderScaledFakeTeammate(
        GuiGraphics graphics,
        int x,
        int y,
        float scale,
        String name,
        int playerColor,
        float health,
        String distance
) {
    graphics.pose().pushPose();

    graphics.pose().translate(
            x,
            y,
            0.0F
    );

    graphics.pose().scale(
            scale,
            scale,
            1.0F
    );

    renderFakeTeammate(
            graphics,
            0,
            0,
            name,
            playerColor,
            health,
            distance
    );

    graphics.pose().popPose();
}

        private int getXOffsetForPosition(
        HudAnchor anchor,
        int x,
        int elementWidth
) {
    return switch (anchor) {

        case TOP_LEFT,
             CENTER_LEFT,
             BOTTOM_LEFT ->
                x;

        case TOP_CENTER,
             CENTER,
             BOTTOM_CENTER ->
                x
                        - (
                        width / 2
                                - elementWidth / 2
                );

        case TOP_RIGHT,
             CENTER_RIGHT,
             BOTTOM_RIGHT ->
                width
                        - elementWidth
                        - x;
    };
}

private int getYOffsetForPosition(
        HudAnchor anchor,
        int y,
        int elementHeight
) {
    return switch (anchor) {

        case TOP_LEFT,
             TOP_CENTER,
             TOP_RIGHT ->
                y;

        case CENTER_LEFT,
             CENTER,
             CENTER_RIGHT ->
                y
                        - (
                        height / 2
                                - elementHeight / 2
                );

        case BOTTOM_LEFT,
             BOTTOM_CENTER,
             BOTTOM_RIGHT ->
                height
                        - elementHeight
                        - y;
    };
}

        private void renderFakeTeammate(
                GuiGraphics graphics,
                int x,
                int y,
                String name,
                int playerColor,
                float health,
                String distance
        ) {
        int width =
                110;

        int height =
                28;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0x88000000
        );

        graphics.fill(
                x + 5,
                y + 5,
                x + 12,
                y + 12,
                playerColor
        );

        graphics.drawString(
                font,
                name,
                x + 16,
                y + 4,
                0xFFFFFFFF,
                true
        );

        int distanceWidth =
                font.width(
                        distance
                );

        graphics.drawString(
                font,
                distance,
                x + width - distanceWidth - 5,
                y + 4,
                0xFFBBBBBB,
                true
        );

        int barX =
                x + 5;

        int barY =
                y + 17;

        int barWidth =
                width - 10;

        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + 6,
                0xFF252525
        );

        graphics.fill(
                barX,
                barY,
                barX
                        + Math.round(
                                barWidth * health
                        ),
                barY + 6,
                0xFF49C95A
        );
        }

    private void renderHeatPreview(
        GuiGraphics graphics
) {
    int previewWidth =
            getHeatPreviewWidth();

    int previewHeight =
            getHeatPreviewHeight();

    int reservedWidth =
                getHeatReservedWidth();

        FloodHudPositioning.Position position =
                FloodHudPositioning.resolveClamped(
                        heatAnchor,
                        width,
                        height,
                        reservedWidth,
                        previewHeight,
                        heatXOffset,
                        heatYOffset
                );

    int x =
            position.x();

    int y =
            position.y();

    graphics.blit(
            FLOOD_HUD_PREVIEW,
            x,
            y,
            previewWidth,
            previewHeight,
            0.0F,
            0.0F,
            FLOOD_HUD_TEXTURE_WIDTH,
            FLOOD_HUD_TEXTURE_HEIGHT,
            FLOOD_HUD_TEXTURE_WIDTH,
            FLOOD_HUD_TEXTURE_HEIGHT
    );

    float scale =
            previewWidth / 220.0F;

    String dayTimeText =
            "DAY 10  •  14:25";

    float textWidth =
            font.width(dayTimeText)
                    * scale;

    int dayTimeX =
            Math.round(
                    x
                            + previewWidth / 2.0F
                            - textWidth / 2.0F
            );

    int dayTimeY =
            y
                    - Math.round(
                    font.lineHeight
                            * scale
            )
                    + Math.round(
                    6.0F * scale
            );

    graphics.pose().pushPose();

    graphics.pose().translate(
            dayTimeX,
            dayTimeY,
            0.0F
    );

    graphics.pose().scale(
            scale,
            scale,
            1.0F
    );

    graphics.drawString(
            font,
            dayTimeText,
            0,
            0,
            0xFFFFFFFF,
            true
    );

    graphics.pose().popPose();

    if (dragging == DragTarget.HEAT) {
        drawSelectionBox(
                graphics,
                x - 2,
                dayTimeY - 2,
                x + previewWidth + 2,
                y + previewHeight + 2
        );
    }
}

    private void drawSelectionBox(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int color = 0xFFFFFFFF;

        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);

        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0) {
            if (isMouseOverHeat(mouseX, mouseY)) {
                dragging = DragTarget.HEAT;
                return true;
            }

            if (isMouseOverTeam(
                        mouseX,
                        mouseY
                )) {
                dragging =
                        DragTarget.TEAM;

                return true;
                }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (button != 0) {
            return super.mouseDragged(
                    mouseX,
                    mouseY,
                    button,
                    dragX,
                    dragY
            );
        }

        if (dragging == DragTarget.HEAT) {

                int elementWidth =
                        getHeatPreviewWidth();

                int elementHeight =
                        getHeatPreviewHeight();

                int x =
                        (int) Math.round(
                                mouseX
                                        - elementWidth / 2.0
                        );

                int y =
                        (int) Math.round(
                                mouseY
                                        - elementHeight / 2.0
                        );

                heatAnchor =
                        getClosestAnchor(
                                mouseX,
                                mouseY
                        );

                heatXOffset =
                        getXOffsetForPosition(
                                heatAnchor,
                                x,
                                elementWidth
                        );

                heatYOffset =
                        getYOffsetForPosition(
                                heatAnchor,
                                y,
                                elementHeight
                        );

                return true;
                }

                if (dragging == DragTarget.TEAM) {

                int elementWidth =
                        getTeamPreviewWidth();

                int elementHeight =
                        getTeamPreviewHeight();

                int x =
                        (int) Math.round(
                                mouseX
                                        - elementWidth / 2.0
                        );

                int y =
                        (int) Math.round(
                                mouseY
                                        - elementHeight / 2.0
                        );

                teamAnchor =
                        getClosestAnchor(
                                mouseX,
                                mouseY
                        );

                teamXOffset =
                        getXOffsetForPosition(
                                teamAnchor,
                                x,
                                elementWidth
                        );

                teamYOffset =
                        getYOffsetForPosition(
                                teamAnchor,
                                y,
                                elementHeight
                        );

                return true;
                }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0 && dragging != DragTarget.NONE) {
            dragging = DragTarget.NONE;
            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    private boolean isMouseOverHeat(
        double mouseX,
        double mouseY
) {
    int previewWidth =
            getHeatPreviewWidth();

    int previewHeight =
            getHeatPreviewHeight();

    int reservedWidth =
                getHeatReservedWidth();

        FloodHudPositioning.Position position =
                FloodHudPositioning.resolveClamped(
                        heatAnchor,
                        width,
                        height,
                        reservedWidth,
                        previewHeight,
                        heatXOffset,
                        heatYOffset
                );

    return mouseX >= position.x()
            && mouseX <= position.x() + previewWidth
            && mouseY >= position.y() - font.lineHeight
            && mouseY <= position.y() + previewHeight;
}

    private boolean isMouseOverTeam(
        double mouseX,
        double mouseY
) {
    int previewWidth =
            getTeamPreviewWidth();

    int previewHeight =
            getTeamPreviewHeight();

    FloodHudPositioning.Position position =
            FloodHudPositioning.resolveClamped(
                    teamAnchor,
                    width,
                    height,
                    previewWidth,
                    previewHeight,
                    teamXOffset,
                    teamYOffset
            );

    return mouseX >= position.x()
            && mouseX <= position.x() + previewWidth
            && mouseY >= position.y()
            && mouseY <= position.y() + previewHeight;
}

    private int normalizedToScreenX(double value) {
        return (int) Math.round(value * width);
    }

    private int normalizedToScreenY(double value) {
        return (int) Math.round(value * height);
    }

    private double screenToNormalizedX(double value) {
        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        value / width
                )
        );
    }

    private double screenToNormalizedY(double value) {
        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        value / height
                )
        );
    }

    private void resetPositions() {
    heatAnchor =
            HudAnchor.TOP_CENTER;

    heatXOffset = 0;
    heatYOffset = 10;

    teamAnchor =
            HudAnchor.TOP_LEFT;

    teamXOffset = 10;
    teamYOffset = 75;
}

    private void saveAndClose() {
    TheFloodClientConfig
            .HEAT_HUD_ANCHOR
            .set(heatAnchor);

    TheFloodClientConfig
            .HEAT_HUD_ANCHOR_X_OFFSET
            .set(heatXOffset);

    TheFloodClientConfig
            .HEAT_HUD_ANCHOR_Y_OFFSET
            .set(heatYOffset);


    TheFloodClientConfig
            .TEAM_HUD_ANCHOR
            .set(teamAnchor);

    TheFloodClientConfig
            .TEAM_HUD_ANCHOR_X_OFFSET
            .set(teamXOffset);

    TheFloodClientConfig
            .TEAM_HUD_ANCHOR_Y_OFFSET
            .set(teamYOffset);

    onClose();
}

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private enum DragTarget {
        NONE,
        HEAT,
        TEAM
    }
}