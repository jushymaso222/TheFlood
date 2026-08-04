package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class HeatHudOverlay {

    private static final int PADDING = 5;
    private static final int PANEL_HEIGHT = 20;
    private static final int NUMBER_OUTLINE_COLOR = 0xFF171717;

    private static final ResourceLocation FLAME_1 =
        new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame1.png");

    private static final ResourceLocation FLAME_2 =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame2.png");

    private static final ResourceLocation FLAME_3 =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame3.png");

    private static final ResourceLocation FLAME_4 =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame4.png");

    private static final ResourceLocation FLAME_5 =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame5.png");

    private static final ResourceLocation FLAME_6 =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/flame6.png");

    private static final int TEXTURE_SIZE = 64;
    private static final int ICON_SIZE = 64;

    private HeatHudOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "heat_hud",
                HeatHudOverlay::render
        );
    }

    private static ResourceLocation getHeatTexture(int heat) {
        if (heat < 8) {
            return FLAME_1;
        }

        if (heat < 16) {
            return FLAME_2;
        }

        if (heat < 25) {
            return FLAME_3;
        }

        if (heat < 35) {
            return FLAME_4;
        }

        if (heat < 50) {
            return FLAME_5;
        }

        return FLAME_6;
    }

    private static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (
                minecraft.player == null
                || minecraft.level == null
                || minecraft.options.hideGui
                || !TheFloodClientConfig.SHOW_HEAT_HUD.get()
        ) {
            return;
        }

        /*
         * Temporary Phase 1 behavior:
         * Heat is equal to the current world day.
         *
         * Later this will become:
         * ClientHeatData.getEffectiveHeat()
         */
        int heat = (int) (
                minecraft.level.getDayTime() / 24_000L
        ) + 1;

        String text = Integer.toString(heat);

        int xOffset =
        TheFloodClientConfig.HEAT_HUD_X_OFFSET.get();

        int yOffset =
                TheFloodClientConfig.HEAT_HUD_Y_OFFSET.get();

        int iconX;
        int iconY;

        switch (TheFloodClientConfig.HEAT_HUD_CORNER.get()) {
            case TOP_LEFT -> {
                iconX = xOffset;
                iconY = yOffset;
            }

            case TOP_RIGHT -> {
                iconX = screenWidth - ICON_SIZE - xOffset;
                iconY = yOffset;
            }

            case BOTTOM_LEFT -> {
                iconX = xOffset;
                iconY = screenHeight - ICON_SIZE - yOffset;
            }

            case BOTTOM_RIGHT -> {
                iconX = screenWidth - ICON_SIZE - xOffset;
                iconY = screenHeight - ICON_SIZE - yOffset;
            }

            default -> {
                iconX = screenWidth - ICON_SIZE - xOffset;
                iconY = yOffset;
            }
        }

        drawHeatIcon(
            graphics,
            minecraft,
            text,
            heat,
            iconX,
            iconY
        );
    }

    private static void drawHeatIcon(
            GuiGraphics graphics,
            Minecraft minecraft,
            String text,
            int heat,
            int iconX,
            int iconY
    ) {
        // Render the 64×64 texture at 40×40 screen pixels.
        ResourceLocation flameTexture = getHeatTexture(heat);

        graphics.blit(
                flameTexture,
                iconX,
                iconY,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        int textWidth = minecraft.font.width(text);

        int textX =
                iconX + ((ICON_SIZE - textWidth) / 2);

        /*
        * Slightly below exact vertical center because most flames have
        * more empty space near their narrow tip.
        */
        int textY =
            iconY + ((ICON_SIZE - minecraft.font.lineHeight) / 2) + 3;

        // Small outline so the number remains visible on bright flames.
        graphics.drawString(
                minecraft.font,
                text,
                textX - 1,
                textY,
                NUMBER_OUTLINE_COLOR,
                false
        );

        graphics.drawString(
                minecraft.font,
                text,
                textX + 1,
                textY,
                NUMBER_OUTLINE_COLOR,
                false
        );

        graphics.drawString(
                minecraft.font,
                text,
                textX,
                textY - 1,
                NUMBER_OUTLINE_COLOR,
                false
        );

        graphics.drawString(
                minecraft.font,
                text,
                textX,
                textY + 1,
                NUMBER_OUTLINE_COLOR,
                false
        );

        graphics.drawString(
                minecraft.font,
                text,
                textX,
                textY,
                getHeatNumberColor(heat),
                true
        );
    }

    private static int getHeatNumberColor(int heat) {
        if (heat >= 35 && heat < 50) {
            // White is more legible against the cyan Soul Flame.
            return 0xFFFFFFFF;
        }

        if (heat >= 50) {
            // Pale white-purple against the magical flame.
            return 0xFFFFEEFF;
        }

        if (heat < 8) {
            return 0xFFFFFF77;
        }

        if (heat < 16) {
            return 0xFFFFD34E;
        }

        if (heat < 25) {
            return 0xFFFFAA32;
        }

        return 0xFFFFE066;
    }
}