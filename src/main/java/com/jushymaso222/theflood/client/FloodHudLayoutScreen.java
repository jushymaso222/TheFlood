package com.jushymaso222.theflood.client;

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

    private static final ResourceLocation HEAT_FLAME =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame1.png"
            );

    private DragTarget dragging = DragTarget.NONE;

    private double heatX;
    private double heatY;

    private double dayX;
    private double dayY;

    public FloodHudLayoutScreen(Screen parent) {
        super(Component.literal("HUD Layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        heatX = TheFloodClientConfig.HEAT_HUD_X.get();
        heatY = TheFloodClientConfig.HEAT_HUD_Y.get();

        dayX = TheFloodClientConfig.DAY_HUD_X.get();
        dayY = TheFloodClientConfig.DAY_HUD_Y.get();

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
        renderDayPreview(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void renderHeatPreview(GuiGraphics graphics) {
        int centerX = normalizedToScreenX(heatX);
        int centerY = normalizedToScreenY(heatY);

        int x = centerX - HEAT_SIZE / 2;
        int y = centerY - HEAT_SIZE / 2;

        graphics.blit(
                HEAT_FLAME,
                x,
                y,
                0,
                0,
                HEAT_SIZE,
                HEAT_SIZE,
                32,
                32
        );

        String heat = "10";

        int textX =
                centerX - font.width(heat) / 2;

        int textY =
                centerY - font.lineHeight / 2 + 3;

        graphics.drawString(
                font,
                heat,
                textX,
                textY,
                0xFFFFFFFF,
                true
        );

        if (dragging == DragTarget.HEAT) {
            drawSelectionBox(
                    graphics,
                    x - 2,
                    y - 2,
                    x + HEAT_SIZE + 2,
                    y + HEAT_SIZE + 2
            );
        }
    }

    private void renderDayPreview(GuiGraphics graphics) {
        String text = "DAY 10 - 14:25";

        int centerX = normalizedToScreenX(dayX);
        int centerY = normalizedToScreenY(dayY);

        int textWidth = font.width(text);

        int x = centerX - textWidth / 2;
        int y = centerY - font.lineHeight / 2;

        graphics.drawString(
                font,
                text,
                x,
                y,
                0xFFFFFFFF,
                true
        );

        if (dragging == DragTarget.DAY) {
            drawSelectionBox(
                    graphics,
                    x - 3,
                    y - 3,
                    x + textWidth + 3,
                    y + font.lineHeight + 3
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

            if (isMouseOverDay(mouseX, mouseY)) {
                dragging = DragTarget.DAY;
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

        if (dragging == DragTarget.DAY) {
            dayX = screenToNormalizedX(mouseX);
            dayY = screenToNormalizedY(mouseY);

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
        int centerX = normalizedToScreenX(heatX);
        int centerY = normalizedToScreenY(heatY);

        int half = HEAT_SIZE / 2;

        return mouseX >= centerX - half
                && mouseX <= centerX + half
                && mouseY >= centerY - half
                && mouseY <= centerY + half;
    }

    private boolean isMouseOverDay(
            double mouseX,
            double mouseY
    ) {
        String text = "DAY 10 - 14:25";

        int centerX = normalizedToScreenX(dayX);
        int centerY = normalizedToScreenY(dayY);

        int textWidth = font.width(text);

        int left = centerX - textWidth / 2;
        int right = centerX + textWidth / 2;

        int top =
                centerY - font.lineHeight / 2;

        int bottom =
                top + font.lineHeight;

        return mouseX >= left
                && mouseX <= right
                && mouseY >= top
                && mouseY <= bottom;
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
        heatX = 0.95;
        heatY = 0.06;

        dayX = 0.50;
        dayY = 0.08;
    }

    private void saveAndClose() {
        TheFloodClientConfig.HEAT_HUD_X.set(heatX);
        TheFloodClientConfig.HEAT_HUD_Y.set(heatY);

        TheFloodClientConfig.DAY_HUD_X.set(dayX);
        TheFloodClientConfig.DAY_HUD_Y.set(dayY);

        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private enum DragTarget {
        NONE,
        HEAT,
        DAY
    }
}