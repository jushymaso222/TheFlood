package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.TheFlood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FloodSettingsIconButton extends Button {

    private static final ResourceLocation ICON =
            new ResourceLocation(
                    TheFlood.MOD_ID,
                    "textures/gui/flame5.png"
            );

    public FloodSettingsIconButton(
            int x,
            int y,
            int width,
            int height,
            OnPress onPress
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.empty(),
                onPress,
                DEFAULT_NARRATION
        );

        setTooltip(
                Tooltip.create(
                        Component.literal("Flood Settings")
                )
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
         * Let Minecraft render the normal button background,
         * including its normal hover behavior.
         */
        super.renderWidget(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        /*
         * Draw the Heat flame centered inside the button.
         *
         * 16x16 gives us a 2px margin inside a 20x20 button.
         */
        int iconSize = 16;

        int iconX =
                getX()
                        + (width - iconSize) / 2;

        int iconY =
                getY()
                        + (height - iconSize) / 2;

        graphics.blit(
                ICON,
                iconX,
                iconY,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        );
    }
}