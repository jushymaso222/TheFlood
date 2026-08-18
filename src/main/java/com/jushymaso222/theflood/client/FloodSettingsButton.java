package com.jushymaso222.theflood.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class FloodSettingsButton extends Button {

    public FloodSettingsButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            OnPress onPress
    ) {
        super(
                x,
                y,
                width,
                height,
                message,
                onPress,
                DEFAULT_NARRATION
        );
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int x =
                getX();

        int y =
                getY();

        int right =
                x + getWidth();

        int bottom =
                y + getHeight();

        /*
         * Subtle Flood-themed tint.
         */
        int background =
                isHoveredOrFocused()
                        ? 0xFF6A4548
                        : 0xFF54383B;

        int topHighlight =
                isHoveredOrFocused()
                        ? 0xFF9A7376
                        : 0xFF806064;

        int darkBorder =
                0xFF1F1516;

        /*
         * Outer border.
         */
        graphics.fill(
                x,
                y,
                right,
                bottom,
                darkBorder
        );

        /*
         * Main button face.
         */
        graphics.fill(
                x + 1,
                y + 1,
                right - 1,
                bottom - 1,
                background
        );

        /*
         * Vanilla-ish top/left highlight.
         */
        graphics.fill(
                x + 1,
                y + 1,
                right - 1,
                y + 2,
                topHighlight
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + 2,
                bottom - 1,
                topHighlight
        );

        /*
         * Bottom/right shadow.
         */
        graphics.fill(
                x + 1,
                bottom - 2,
                right - 1,
                bottom - 1,
                darkBorder
        );

        graphics.fill(
                right - 2,
                y + 1,
                right - 1,
                bottom - 1,
                darkBorder
        );

        /*
        * Vanilla-style white hover outline.
        */
        if (isHoveredOrFocused()) {

            int hoverBorder =
                    0xFFFFFFFF;

            // Top
            graphics.fill(
                    x,
                    y,
                    right,
                    y + 1,
                    hoverBorder
            );

            // Bottom
            graphics.fill(
                    x,
                    bottom - 1,
                    right,
                    bottom,
                    hoverBorder
            );

            // Left
            graphics.fill(
                    x,
                    y,
                    x + 1,
                    bottom,
                    hoverBorder
            );

            // Right
            graphics.fill(
                    right - 1,
                    y,
                    right,
                    bottom,
                    hoverBorder
            );
        }

        /*
         * Centered button text.
         */
        Minecraft minecraft =
                Minecraft.getInstance();

        int textColor =
                active
                        ? 0xFFFFFFFF
                        : 0xFFA0A0A0;

        graphics.drawCenteredString(
                minecraft.font,
                getMessage(),
                x + getWidth() / 2,
                y
                        + (
                        getHeight() - 8
                ) / 2,
                textColor
        );
    }
}