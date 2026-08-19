package com.jushymaso222.theflood.team.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class TeamTabButton extends AbstractButton {

    private final ResourceLocation icon;

    private final Consumer<TeamTabButton> onPress;

    public TeamTabButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            ResourceLocation icon,
            Consumer<TeamTabButton> onPress
    ) {
        super(
                x,
                y,
                width,
                height,
                message
        );

        this.icon =
                icon;

        this.onPress =
                onPress;
    }

    @Override
    public void onPress() {
        onPress.accept(
                this
        );
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Creative-tab-style background.
         */
        int background =
                isHoveredOrFocused()
                        ? 0xFFE0E0E0
                        : 0xFFC6C6C6;

        int darkBorder =
                0xFF555555;

        int lightBorder =
                0xFFFFFFFF;

        int visibleRight =
            getX() + width - 2;

        int bodyRight =
            getX() + width - 1;

        /*
         * Outer dark border.
         */
        graphics.fill(
                getX(),
                getY(),
                bodyRight,
                getY() + height,
                darkBorder
        );

        /*
         * Inner tab face.
         */
        graphics.fill(
                getX() + 1,
                getY() + 1,
                bodyRight,
                getY() + height - 1,
                background
        );

        /*
         * Vanilla-ish highlight across the top
         * and left side.
         */
        graphics.fill(
                getX() + 1,
                getY() + 1,
                bodyRight,
                getY() + 2,
                lightBorder
        );

        graphics.fill(
                getX() + 1,
                getY() + 1,
                getX() + 2,
                getY() + height - 1,
                lightBorder
        );

        int overlapX =
            getX() + width - 1;
        

        /*
        * One-pixel overlap used to visually bridge over
        * Minecraft's black inventory border.
        *
        * Only draw the top and bottom tab border colors
        * on this column so the inventory still appears
        * to sit above the tab.
        */
        graphics.fill(
                overlapX,
                getY() + 2,
                overlapX + 1,
                getY() + 2,
                lightBorder
        );

        graphics.fill(
                overlapX,
                getY() + height - 2,
                overlapX + 1,
                getY() + height - 2,
                darkBorder
        );

        /*
         * Center the 16x16 Teams icon.
         */
        int iconX =
                getX()
                        + (
                        width - 16
                ) / 2;

        int iconY =
                getY()
                        + (
                        height - 16
                ) / 2;

        RenderSystem.enableBlend();

        graphics.blit(
                icon,
                iconX,
                iconY,
                0,
                0,
                16,
                16,
                16,
                16
        );

        RenderSystem.disableBlend();
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput narration
    ) {
        defaultButtonNarrationText(
                narration
        );
    }
}