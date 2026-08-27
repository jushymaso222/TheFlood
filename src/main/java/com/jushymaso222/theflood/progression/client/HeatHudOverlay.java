package com.jushymaso222.theflood.progression.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.jushymaso222.theflood.time.BloodMoonState;

import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class HeatHudOverlay {

    private static final int PADDING = 5;
    private static final int PANEL_HEIGHT = 20;

    private static final ResourceLocation HUD_FRAME =
        new ResourceLocation(
                TheFlood.MOD_ID,
                "textures/gui/progression/floodxp.png"
        );

        private static final ResourceLocation SEGMENT =
        new ResourceLocation(
                TheFlood.MOD_ID,
                "textures/gui/progression/floodxp_segment.png"
        );

        private static float[] getHudTint(int heat) {
        if (heat >= 80) {
                return new float[] { 0.68F, 0.32F, 0.85F };
        }

        if (heat >= 60) {
                return new float[] { 0.82F, 0.25F, 0.25F };
        }

        if (heat >= 40) {
                return new float[] { 0.90F, 0.50F, 0.18F };
        }

        if (heat >= 20) {
                return new float[] { 0.20F, 0.62F, 0.82F };
        }

        return new float[] { 0.20F, 0.72F, 0.43F };
        }

        private static final int TEXTURE_WIDTH =
                629;

        private static final int TEXTURE_HEIGHT =
                133;

        /*
        * Starting display size.
        *
        * We can tune this after seeing it at top-center.
        */
        private static final float HUD_WIDTH_RATIO =
                220.0F / (2560.0F / 3.0F);

        private static final int SEGMENT_COUNT =
                7;

        private static int getHeatNumberOutlineColor(int heat) {
    if (heat >= 80) {
        // Vibrant purple
        return 0xFFA94FD1;
    }

    if (heat >= 60) {
        // Vibrant red
        return 0xFFD94A4A;
    }

    if (heat >= 40) {
        // Vibrant orange
        return 0xFFE58A32;
    }

    if (heat >= 20) {
        // Vibrant cyan
        return 0xFF3AADD1;
    }

    // Vibrant emerald
    return 0xFF3BC978;
}

        private static final int SEGMENT_SOURCE_X =
                128;

        private static final int SEGMENT_SOURCE_Y =
                53;

        private static final int SEGMENT_SOURCE_WIDTH =
                59;

        private static final int SEGMENT_SOURCE_HEIGHT =
                26;

        /*
        * The slots are effectively adjacent in the source artwork.
        */
        private static final int SEGMENT_SOURCE_STEP =
                59;

    private HeatHudOverlay() {
    }

        private static int getHudWidth(int screenWidth) {
    return Math.round(
            screenWidth * HUD_WIDTH_RATIO
    );
}

private static int getHudHeight(int hudWidth) {
    return Math.round(
            hudWidth
                    * (
                    TEXTURE_HEIGHT
                            / (float) TEXTURE_WIDTH
            )
    );
}

private static float getHudScale(int hudWidth) {
    return hudWidth
            / (float) TEXTURE_WIDTH;
}

private static int scaleX(
        int sourcePixels,
        int hudWidth
) {
    return Math.round(
            sourcePixels
                    * getHudScale(hudWidth)
    );
}


    private static void renderProgress(
        GuiGraphics graphics,
        int barX,
        int barY,
        float progress,
        int heat,
        int hudWidth
) {
    /*
     * Temporary coordinates.
     *
     * These came from our original 182px version,
     * so we'll tune them against the new 220px HUD
     * once we see it in-game.
     */
    int firstSegmentX =
                scaleX(
                        SEGMENT_SOURCE_X, hudWidth
                );

        int segmentY =
                scaleX(
                        SEGMENT_SOURCE_Y, hudWidth
                );

        int segmentWidth =
                scaleX(
                        SEGMENT_SOURCE_WIDTH, hudWidth
                );

        int segmentHeight =
                scaleX(
                        SEGMENT_SOURCE_HEIGHT, hudWidth
                );

        int segmentStep =
                scaleX(
                        SEGMENT_SOURCE_STEP, hudWidth
                );


    float segmentProgress =
            progress
                    * SEGMENT_COUNT;

    int completeSegments =
            (int) Math.floor(
                    segmentProgress
            );

    float partial =
            segmentProgress
                    - completeSegments;


    float[] color =
            getFillColor(
                    heat
            );

    RenderSystem.setShaderColor(
            color[0],
            color[1],
            color[2],
            1.0F
    );


    for (
            int i = 0;
            i < SEGMENT_COUNT;
            i++
    ) {
        int segmentX =
        barX
                + firstSegmentX
                + Math.round(
                        i
                                * SEGMENT_SOURCE_STEP
                                * getHudScale(hudWidth)
                );


        if (i < completeSegments) {
            graphics.blit(
                    SEGMENT,
                    segmentX,
                    barY + segmentY,
                    0,
                    0,
                    segmentWidth,
                    segmentHeight,
                    segmentWidth,
                    segmentHeight
            );

            continue;
        }


        if (
                i == completeSegments
                && partial > 0.0F
        ) {
            int visibleWidth =
                    Math.max(
                            1,
                            Math.round(
                                    segmentWidth
                                            * partial
                            )
                    );

            graphics.blit(
                    SEGMENT,
                    segmentX,
                    barY + segmentY,
                    0,
                    0,
                    visibleWidth,
                    segmentHeight,
                    segmentWidth,
                    segmentHeight
            );
        }

        break;
    }


    RenderSystem.setShaderColor(
            1.0F,
            1.0F,
            1.0F,
            1.0F
    );
}

private static float[] getFillColor(int heat) {
    if (heat >= 80) {
        return new float[] {
                0.85F,
                0.30F,
                1.00F
        };
    }

    if (heat >= 60) {
        return new float[] {
                1.00F,
                0.25F,
                0.25F
        };
    }

    if (heat >= 40) {
        return new float[] {
                1.00F,
                0.60F,
                0.14F
        };
    }

    if (heat >= 20) {
        return new float[] {
                0.16F,
                0.84F,
                1.00F
        };
    }

    return new float[] {
            0.21F,
            1.00F,
            0.54F
    };
}

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "heat_hud",
                HeatHudOverlay::render
        );
    }

    private static void render(
        ForgeGui gui,
        GuiGraphics graphics,
        float partialTick,
        int screenWidth,
        int screenHeight
) {
    Minecraft minecraft =
            Minecraft.getInstance();

    if (
            minecraft.player == null
            || minecraft.level == null
            || minecraft.options.hideGui
            || !TheFloodClientConfig.SHOW_HEAT_HUD.get()
    ) {
        return;
    }

    int hudWidth =
                getHudWidth(screenWidth);

        int hudHeight =
                getHudHeight(hudWidth);

    int heat =
            ClientHeatData.getEffectiveHeat();

    int proximityBonus =
            ClientHeatData.getProximityBonus();


    /*
     * =================================================
     * TEST FLOOD XP
     * =================================================
     *
     * For now, one bar fill per Minecraft day.
     */

    long dayTime =
            minecraft.level.getDayTime()
                    % 24000L;

    float progress =
            dayTime
                    / 24000.0F;

    progress =
            Math.max(
                    0.0F,
                    Math.min(
                            1.0F,
                            progress
                    )
            );


    /*
     * =================================================
     * CONFIGURABLE POSITION
     * =================================================
     */

    int xOffset =
            TheFloodClientConfig
                    .HEAT_HUD_X_OFFSET
                    .get();

    int yOffset =
            TheFloodClientConfig
                    .HEAT_HUD_Y_OFFSET
                    .get();

    int centerX =
            (int) Math.round(
                    screenWidth
                            * TheFloodClientConfig
                            .HEAT_HUD_X
                            .get()
            )
            + xOffset;

    int centerY =
            (int) Math.round(
                    screenHeight
                            * TheFloodClientConfig
                            .HEAT_HUD_Y
                            .get()
            )
            + yOffset;


    int barX =
        centerX
                - hudWidth / 2;

    int barY =
            centerY;


    drawFloodHud(
                graphics,
                minecraft,
                heat,
                proximityBonus,
                progress,
                barX,
                barY,
                hudWidth,
                hudHeight
        );
}

    private static void drawFloodHud(
        GuiGraphics graphics,
        Minecraft minecraft,
        int heat,
        int proximityBonus,
        float progress,
        int barX,
        int barY,
        int hudWidth,
        int hudHeight
) {
    ResourceLocation frame =
            HUD_FRAME;

    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();


    /*
     * =================================================
     * DAY / TIME
     * =================================================
     */

    long absoluteDayTime =
            minecraft.level.getDayTime();

    long day =
            absoluteDayTime
                    / 24000L
                    + 1L;

    long timeOfDay =
            absoluteDayTime
                    % 24000L;

    boolean bloodMoon =
        BloodMoonState.isBloodMoon(
                minecraft.level.getDayTime()
        );

    /*
     * Minecraft tick 0 = 6:00 AM.
     */
    int totalMinutes =
            (int) (
                    (
                            timeOfDay
                                    + 6000L
                    )
                            % 24000L
            )
                    * 1440
                    / 24000;

    int hours =
            totalMinutes
                    / 60;

    int minutes =
            totalMinutes
                    % 60;

    String timeText =
            String.format(
                    "%02d:%02d",
                    hours,
                    minutes
            );

        String dayTimeText =
        "DAY "
                + day
                + "  •  "
                + timeText;

    float textScale =
        hudWidth / 220.0F;

int dayTimeWidth =
        Math.round(
                minecraft.font.width(dayTimeText)
                        * textScale
        );

int dayTimeX =
        barX
                + hudWidth / 2
                - dayTimeWidth / 2;

    int dayTimeY =
            barY
                    - minecraft.font.lineHeight
                    + 6;

    graphics.pose().pushPose();

        graphics.pose().translate(
                dayTimeX,
                dayTimeY,
                0.0F
        );

        graphics.pose().scale(
                textScale,
                textScale,
                1.0F
        );

        graphics.drawString(
                minecraft.font,
                dayTimeText,
                0,
                0,
                bloodMoon
                        ? 0xFFFF3333
                        : 0xFFFFFFFF,
                true
        );

        graphics.pose().popPose();


    /*
     * =================================================
     * FRAME
     * =================================================
     */

    float[] hudColor = getHudTint(heat);

        RenderSystem.setShaderColor(
                hudColor[0],
                hudColor[1],
                hudColor[2],
                1.0F
        );

        graphics.blit(
                HUD_FRAME,
                barX,
                barY,
                hudWidth,
                hudHeight,
                0.0F,
                0.0F,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );


    /*
     * =================================================
     * XP FILL
     * =================================================
     */

    renderProgress(
        graphics,
        barX,
        barY,
        progress,
        heat,
        hudWidth
);


    /*
     * =================================================
     * HEAT NUMBER
     * =================================================
     */

    String heatText =
            Integer.toString(
                    heat
            );

    /*
     * Temporary values based on the scaled artwork.
     * We'll tune these after one screenshot.
     */
    int diamondCenterX =
                barX
                        + scaleX(
                                72, hudWidth
                        );

        int diamondCenterY =
                barY
                        + 2
                        + hudHeight / 2;

        int heatX =
                diamondCenterX
                        - Math.round(
                                minecraft.font.width(heatText)
                                        * textScale
                                        / 2.0F
                        );

        int heatY =
                diamondCenterY
                        - Math.round(
                                minecraft.font.lineHeight
                                        * textScale
                                        / 2.0F
                        );


    drawOutlinedText(
                graphics,
                minecraft,
                heatText,
                heatX,
                heatY,
                getHeatNumberColor(heat),
                getHeatNumberOutlineColor(heat),
                hudWidth
        );


    /*
     * =================================================
     * PROXIMITY BONUS
     * =================================================
     *
     * Don't lose the information the old Heat HUD showed.
     */

    if (proximityBonus > 0) {
        String bonusText =
                "+"
                        + proximityBonus;

        int bonusX =
                diamondCenterX
                        - minecraft.font.width(
                                bonusText
                        ) / 2;

        int bonusY =
                barY
                        + hudHeight
                        - 3;

        graphics.drawString(
                minecraft.font,
                bonusText,
                bonusX,
                bonusY,
                0xFFFFAA55,
                true
        );
    }


    RenderSystem.disableBlend();
}

private static int getHeatNumberColor(int heat) {
    if (heat >= 80) {
        return 0xFFF5E8FF; // pale purple-white
    }

    if (heat >= 60) {
        return 0xFFFFE8E8; // pale red-white
    }

    if (heat >= 40) {
        return 0xFFFFF0DF; // pale orange-white
    }

    if (heat >= 20) {
        return 0xFFE5F8FF; // pale cyan-white
    }

    return 0xFFE6FFEE;     // pale green-white
}

private static int getHeatNeonColor(int heat) {
    if (heat >= 80) {
        // Neon purple
        return 0xFFD94CFF;
    }

    if (heat >= 60) {
        // Neon red
        return 0xFFFF4040;
    }

    if (heat >= 40) {
        // Neon orange
        return 0xFFFF9A24;
    }

    if (heat >= 20) {
        // Neon cyan/blue
        return 0xFF28D7FF;
    }

    // Neon green
    return 0xFF35FF8A;
}

private static void drawOutlinedText(
        GuiGraphics graphics,
        Minecraft minecraft,
        String text,
        int x,
        int y,
        int color,
        int outlineColor,
        int hudWidth
) {
    float textScale =
            hudWidth / 220.0F;

    graphics.pose().pushPose();

    graphics.pose().translate(
            x,
            y,
            0.0F
    );

    graphics.pose().scale(
            textScale,
            textScale,
            1.0F
    );

    for (int offsetX = -1; offsetX <= 1; offsetX++) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            if (offsetX == 0 && offsetY == 0) {
                continue;
            }

            graphics.drawString(
                    minecraft.font,
                    text,
                    offsetX,
                    offsetY,
                    outlineColor,
                    false
            );
        }
    }

    graphics.drawString(
            minecraft.font,
            text,
            0,
            0,
            color,
            false
    );

    graphics.pose().popPose();
}
}