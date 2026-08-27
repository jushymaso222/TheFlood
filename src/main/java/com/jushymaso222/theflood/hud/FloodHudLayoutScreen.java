package com.jushymaso222.theflood.hud;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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

        private static final int FLOOD_HUD_PREVIEW_WIDTH = 220;

        private static final int FLOOD_HUD_PREVIEW_HEIGHT =
                Math.round(
                        FLOOD_HUD_PREVIEW_WIDTH
                                * (
                                FLOOD_HUD_TEXTURE_HEIGHT
                                        / (float) FLOOD_HUD_TEXTURE_WIDTH
                        )
                );

    private DragTarget dragging = DragTarget.NONE;

    private double heatX;
    private double heatY;

    private double teamX;
        private double teamY;

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
        heatX = TheFloodClientConfig.HEAT_HUD_X.get();
        heatY = TheFloodClientConfig.HEAT_HUD_Y.get();

        teamX =
                TheFloodClientConfig.TEAM_HUD_X.get();

        teamY =
                TheFloodClientConfig.TEAM_HUD_Y.get();

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

    private void renderTeamPreview(
                GuiGraphics graphics
        ) {
        int x =
                normalizedToScreenX(teamX);

        int y =
                normalizedToScreenY(teamY);

        renderFakeTeammate(
                graphics,
                x,
                y,
                "Bob",
                0xFF64B5F6,
                0.75F,
                "32m"
        );

        renderFakeTeammate(
                graphics,
                x,
                y + 32,
                "Alex",
                0xFFE57373,
                0.45F,
                "57m"
        );
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
    int centerX =
            normalizedToScreenX(heatX);

    int centerY =
            normalizedToScreenY(heatY);

    int x =
            centerX
                    - FLOOD_HUD_PREVIEW_WIDTH / 2;

    int y =
            centerY;

    graphics.blit(
            FLOOD_HUD_PREVIEW,
            x,
            y,
            FLOOD_HUD_PREVIEW_WIDTH,
            FLOOD_HUD_PREVIEW_HEIGHT,
            0.0F,
            0.0F,
            FLOOD_HUD_TEXTURE_WIDTH,
            FLOOD_HUD_TEXTURE_HEIGHT,
            FLOOD_HUD_TEXTURE_WIDTH,
            FLOOD_HUD_TEXTURE_HEIGHT
    );

    /*
     * Example day/time so the preview represents
     * the actual combined HUD.
     */
    String dayTimeText =
            "DAY 10  •  14:25";

    int dayTimeX =
            centerX
                    - font.width(dayTimeText) / 2;

    int dayTimeY =
            y
                    - font.lineHeight
                    + 2;

    graphics.drawString(
            font,
            dayTimeText,
            dayTimeX,
            dayTimeY,
            0xFFFFFFFF,
            true
    );

    if (dragging == DragTarget.HEAT) {
        drawSelectionBox(
                graphics,
                x - 2,
                dayTimeY - 2,
                x + FLOOD_HUD_PREVIEW_WIDTH + 2,
                y + FLOOD_HUD_PREVIEW_HEIGHT + 2
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
            heatX = screenToNormalizedX(mouseX);
            heatY = screenToNormalizedY(mouseY);

            return true;
        }

        if (dragging == DragTarget.TEAM) {

                teamX =
                        screenToNormalizedX(
                                mouseX
                        );

                teamY =
                        screenToNormalizedY(
                                mouseY
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
    int centerX =
            normalizedToScreenX(heatX);

    int centerY =
            normalizedToScreenY(heatY);

    int left =
            centerX
                    - FLOOD_HUD_PREVIEW_WIDTH / 2;

    int right =
            left
                    + FLOOD_HUD_PREVIEW_WIDTH;

    int top =
            centerY
                    - font.lineHeight
                    + 2;

    int bottom =
            centerY
                    + FLOOD_HUD_PREVIEW_HEIGHT;

    return mouseX >= left
            && mouseX <= right
            && mouseY >= top
            && mouseY <= bottom;
}

    private boolean isMouseOverTeam(
                double mouseX,
                double mouseY
        ) {
        int x =
                normalizedToScreenX(
                        teamX
                );

        int y =
                normalizedToScreenY(
                        teamY
                );

        return mouseX >= x
                && mouseX <= x
                        + TEAM_PREVIEW_WIDTH
                && mouseY >= y
                && mouseY <= y
                        + TEAM_PREVIEW_HEIGHT;
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
        heatX = 0.50;
        heatY = 0.06;

        teamX = 0.02;
        teamY = 0.35;
    }

    private void saveAndClose() {
        TheFloodClientConfig.HEAT_HUD_X.set(heatX);
        TheFloodClientConfig.HEAT_HUD_Y.set(heatY);

        TheFloodClientConfig.TEAM_HUD_X.set(
                teamX
        );
        TheFloodClientConfig.TEAM_HUD_Y.set(
                teamY
        );

        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private enum DragTarget {
        NONE,
        HEAT,
        DAY,
        TEAM
    }
}