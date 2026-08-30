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
import com.jushymaso222.theflood.hud.FloodTabOverlay;
import com.jushymaso222.theflood.progression.HeatTier;

import com.jushymaso222.theflood.hud.FloodHudPositioning;

import com.jushymaso222.theflood.progression.milestone.client.ClientMilestoneNotifications;

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

        private static int getHeatNumberOutlineColor(
                int heat
        ) {
        return HeatTier.getColor(
                heat
        );
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
                || FloodTabOverlay.isTabOpen()
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
        ClientHeatData.getFloodXpProgress();


    /*
     * =================================================
     * CONFIGURABLE POSITION
     * =================================================
     */

        FloodHudPositioning.Position hudPosition =
                FloodHudPositioning.resolveClamped(
                        TheFloodClientConfig
                                .HEAT_HUD_ANCHOR
                                .get(),
                        screenWidth,
                        screenHeight,
                        hudWidth,
                        hudHeight,
                        TheFloodClientConfig
                                .HEAT_HUD_ANCHOR_X_OFFSET
                                .get(),
                        TheFloodClientConfig
                                .HEAT_HUD_ANCHOR_Y_OFFSET
                                .get()
                );

        int barX =
                hudPosition.x();

        int barY =
                hudPosition.y();

        int centerX =
                barX
                        + hudWidth / 2;

        int centerY =
                barY;


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

        renderMilestoneNotification(
                graphics,
                minecraft,
                heat,
                barX,
                barY,
                hudWidth,
                hudHeight,
                screenHeight
        );

        if (
        ClientFloodXpGain.isVisible()
) {
    long gainedXp =
            ClientFloodXpGain.getAccumulatedXp();

    float alpha =
            ClientFloodXpGain.getAlpha();

    String text =
            "+"
                    + gainedXp;

    int alphaByte =
            Math.round(
                    255.0F
                            * alpha
            );

    int color =
            getHeatNumberOutlineColor(heat);

    int xpTextX =
            barX
                    - 10
                    + hudWidth
                    + scaleX(
                            6,
                            hudWidth
                    );

    int xpTextY =
            centerY
                    + minecraft.font.lineHeight / 2
                    + 15;

    graphics.drawString(
            minecraft.font,
            text,
            xpTextX,
            xpTextY,
            color,
            true
    );
}
}

        private static void renderMilestoneNotification(
        GuiGraphics graphics,
        Minecraft minecraft,
        int heat,
        int barX,
        int barY,
        int hudWidth,
        int hudHeight,
        int screenHeight
) {
    ClientMilestoneNotifications.Notification notification =
            ClientMilestoneNotifications.getCurrent();

    if (notification == null) {
        return;
    }


    /*
     * ============================================
     * ANIMATION
     * ============================================
     */

    float progress =
            ClientMilestoneNotifications.getProgress();

    float alpha;

    if (progress < 0.15F) {
        /*
         * Fade in.
         */
        alpha =
                progress
                        / 0.15F;
    }
    else if (progress > 0.80F) {
        /*
         * Fade out.
         */
        alpha =
                1.0F
                        - (
                                (progress - 0.80F)
                                        / 0.20F
                        );
    }
    else {
        alpha =
                1.0F;
    }

    alpha =
            Math.max(
                    0.0F,
                    Math.min(
                            1.0F,
                            alpha
                    )
            );


    /*
     * ============================================
     * SCALE
     * ============================================
     *
     * Same scaling system as the Flood HUD.
     */

    float textScale =
            hudWidth
                    / 220.0F;


    /*
     * ============================================
     * TEXT
     * ============================================
     */

    String header =
            "MILESTONE REACHED";

    String title =
            notification.title()
                    .getString();

    String description =
            notification.description()
                    .getString();

    String reward =
            "+"
                    + Math.round(
                            notification.floodXpReward()
                                    * 100.0D
                    )
                    + "% FLOOD XP";


    /*
     * ============================================
     * DIMENSIONS
     * ============================================
     */

    int padding =
            Math.max(
                    4,
                    Math.round(
                            5.0F
                                    * textScale
                    )
            );

    int lineSpacing =
            Math.max(
                    1,
                    Math.round(
                            2.0F
                                    * textScale
                    )
            );

    int lineHeight =
            Math.round(
                    minecraft.font.lineHeight
                            * textScale
            );

    int contentWidth =
            Math.max(
                    Math.max(
                            minecraft.font.width(
                                    header
                            ),
                            minecraft.font.width(
                                    title
                            )
                    ),
                    Math.max(
                            minecraft.font.width(
                                    description
                            ),
                            minecraft.font.width(
                                    reward
                            )
                    )
            );

    contentWidth =
            Math.round(
                    contentWidth
                            * textScale
            );

    int notificationWidth =
            Math.max(
                    Math.round(
                            hudWidth
                                    * 0.75F
                    ),
                    contentWidth
                            + padding * 2
            );

    /*
     * Four lines.
     */
    int notificationHeight =
            padding * 2
                    + lineHeight * 4
                    + lineSpacing * 3;


    /*
     * ============================================
     * POSITION
     * ============================================
     */

    int centerX =
            barX
                    + hudWidth / 2;

    int notificationX =
            centerX
                    - notificationWidth / 2;

    int gap =
            Math.max(
                    3,
                    Math.round(
                            4.0F
                                    * textScale
                    )
            );

    int notificationY =
            barY
                    + hudHeight
                    + gap;


    /*
     * If the player has positioned the Flood HUD
     * near the bottom of the screen, put the
     * milestone above it instead.
     */

    if (
            notificationY
                    + notificationHeight
                    > screenHeight - 2
    ) {
        notificationY =
                barY
                        - notificationHeight
                        - gap;
    }


    /*
     * ============================================
     * SLIDE
     * ============================================
     */

    int slideDistance =
            Math.max(
                    2,
                    Math.round(
                            5.0F
                                    * textScale
                    )
            );

    int slideOffset =
            Math.round(
                    (1.0F - alpha)
                            * slideDistance
            );

    /*
     * Below HUD = slide downward/up into place.
     * Above HUD = opposite direction.
     */

    if (notificationY > barY) {
        notificationY +=
                slideOffset;
    }
    else {
        notificationY -=
                slideOffset;
    }


    /*
     * ============================================
     * COLORS
     * ============================================
     */

    int alphaByte =
            Math.round(
                    alpha
                            * 255.0F
            );

    int backgroundAlpha =
            Math.round(
                    alpha
                            * 145.0F
            );

    int backgroundColor =
            backgroundAlpha
                    << 24;

    int heatColor =
            getHeatNeonColor(
                    heat
            );

    int heatRgb =
            heatColor
                    & 0x00FFFFFF;

    int coloredText =
            (alphaByte << 24)
                    | heatRgb;

    int whiteText =
            (alphaByte << 24)
                    | 0x00FFFFFF;


    /*
     * ============================================
     * BACKGROUND
     * ============================================
     */

    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();

    graphics.fill(
            notificationX,
            notificationY,
            notificationX
                    + notificationWidth,
            notificationY
                    + notificationHeight,
            backgroundColor
    );


    /*
     * Thin Heat-colored accents.
     *
     * This ties it visually to the existing
     * Flood HUD without creating another giant
     * framed Minecraft panel.
     */

    graphics.fill(
            notificationX,
            notificationY,
            notificationX
                    + notificationWidth,
            notificationY + 1,
            coloredText
    );

    graphics.fill(
            notificationX,
            notificationY
                    + notificationHeight
                    - 1,
            notificationX
                    + notificationWidth,
            notificationY
                    + notificationHeight,
            coloredText
    );


    /*
     * ============================================
     * TEXT
     * ============================================
     */

    int textY =
            notificationY
                    + padding;

    drawCenteredScaledText(
            graphics,
            minecraft,
            header,
            centerX,
            textY,
            coloredText,
            textScale
    );

    textY +=
            lineHeight
                    + lineSpacing;

    drawCenteredScaledText(
            graphics,
            minecraft,
            title,
            centerX,
            textY,
            whiteText,
            textScale
    );

    textY +=
            lineHeight
                    + lineSpacing;

    drawCenteredScaledText(
            graphics,
            minecraft,
            description,
            centerX,
            textY,
            whiteText,
            textScale
    );

    textY +=
            lineHeight
                    + lineSpacing;

    drawCenteredScaledText(
            graphics,
            minecraft,
            reward,
            centerX,
            textY,
            coloredText,
            textScale
    );

    RenderSystem.disableBlend();
}

        private static void drawCenteredScaledText(
        GuiGraphics graphics,
        Minecraft minecraft,
        String text,
        int centerX,
        int y,
        int color,
        float scale
) {
    int width =
            Math.round(
                    minecraft.font.width(
                            text
                    )
                            * scale
            );

    int x =
            centerX
                    - width / 2;

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

    graphics.drawString(
            minecraft.font,
            text,
            0,
            0,
            color,
            true
    );

    graphics.pose().popPose();
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
                                72,
                                hudWidth
                        );

        int diamondCenterY =
                barY
                        + hudHeight / 2
                        + scaleX(
                                2,
                                hudWidth
                        );

        float scaledTextWidth =
                minecraft.font.width(heatText)
                        * textScale;

        float scaledTextHeight =
                minecraft.font.lineHeight
                        * textScale;

        int heatX =
                Math.round(
                        diamondCenterX
                                - scaledTextWidth / 2.0F
                );

        int heatY =
                Math.round(
                        diamondCenterY
                                - scaledTextHeight / 2.0F
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

private static int getHeatNeonColor(
        int heat
) {
    return HeatTier.getColor(
            heat
    );
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