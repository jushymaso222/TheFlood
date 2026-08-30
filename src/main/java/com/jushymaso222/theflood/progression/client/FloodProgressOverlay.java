package com.jushymaso222.theflood.progression.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import com.jushymaso222.theflood.progression.HeatTier;

import com.jushymaso222.theflood.progression.scaling.MobScaling;

import java.util.List;

public final class FloodProgressOverlay {

    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 100;

    private static int lightenColor(int color) {
    int red = Math.min(255, ((color >> 16) & 0xFF) + 40);
    int green = Math.min(255, ((color >> 8) & 0xFF) + 40);
    int blue = Math.min(255, (color & 0xFF) + 40);

    return 0xFF000000
            | (red << 16)
            | (green << 8)
            | blue;
}

    /*
     * We will add these image files later:
     *
     * assets/theflood/textures/gui/mobs/zombie.png
     * assets/theflood/textures/gui/mobs/skeleton.png
     * assets/theflood/textures/gui/mobs/spider.png
     * assets/theflood/textures/gui/mobs/creeper.png
     * assets/theflood/textures/gui/mobs/enderman.png
     * assets/theflood/textures/gui/mobs/warden.png
     */
    private static final ResourceLocation ZOMBIE_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/zombie.png");

    private static final ResourceLocation SKELETON_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/skeleton.png");

    private static final ResourceLocation SPIDER_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/spider.png");

    private static final ResourceLocation CREEPER_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/creeper.png");

    private static final ResourceLocation ENDERMAN_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/enderman.png");

    private static final ResourceLocation WARDEN_ICON =
            new ResourceLocation(TheFlood.MOD_ID, "textures/gui/mobs/warden.png");

    private FloodProgressOverlay() {
    }

    private static void render(
            net.minecraftforge.client.gui.overlay.ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        // Only display while the normal player-list key is held.
        if (!minecraft.options.keyPlayerList.isDown()) {
            return;
        }

        int currentHeat = ClientHeatData.getEffectiveHeat();

        List<MobProgress> mobs = createMobProgressList();

        int panelX = (screenWidth - PANEL_WIDTH) / 2;

        /*
         * Temporary fixed position.
         *
         * Once the panel works, we can calculate a more exact position based
         * on the number of players currently shown in the vanilla Tab list.
         */
        int panelY = 82;
    }

    public static int renderSection(
        GuiGraphics graphics,
        Minecraft minecraft,
        int currentHeat,
        int x,
        int y,
        int width
) {
    List<MobProgress> mobs =
            createMobProgressList();

    int sectionHeight = 108;

        int barY =
                y + 60;

    /*
     * Title
     */
    graphics.drawString(
            minecraft.font,
            "FLOOD PROGRESSION",
            x + 8,
            y,
            0xFFFFFFFF,
            true
    );

    graphics.fill(
                x + 8,
                y + 13,
                x + 50,
                y + 14,
                HeatTier.getColor(
                        currentHeat
                )
        );

    /*
     * Main progression area.
     */
    int barX =
            x + 24;

    int barWidth =
            width - 48;

    drawProgressRail(
            graphics,
            mobs,
            currentHeat,
            barX,
            barY,
            barWidth
    );

    drawMobMarkers(
            graphics,
            minecraft,
            mobs,
            currentHeat,
            barX,
            barY,
            barWidth
    );

    drawCurrentHeatMarker(
            graphics,
            minecraft,
            mobs,
            currentHeat,
            barX,
            barY,
            barWidth
    );

    drawUnknownFuture(
            graphics,
            minecraft,
            mobs,
            barX,
            barY,
            barWidth
    );

    return y + sectionHeight;
}

    private static void drawProgressRail(
        GuiGraphics graphics,
        List<MobProgress> mobs,
        int currentHeat,
        int x,
        int y,
        int width
) {
    if (mobs.isEmpty()) {
        return;
    }

    int finalUnlockHeat =
        mobs.stream()
                .mapToInt(
                        MobProgress::unlockHeat
                )
                .max()
                .orElse(1);

        int knownWidth =
                Math.round(
                        width * 0.82F
                );

        float progress =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                currentHeat
                                        / (float) finalUnlockHeat
                        )
                );

        int filledWidth =
                Math.round(
                        knownWidth * progress
                );

    /*
     * Base rail.
     */
    graphics.fill(
            x,
            y,
            x + knownWidth,
            y + 2,
            0xFF3A3A3A
    );

    /*
     * Current known progress.
     */
    double knownProgress =
            Math.max(
                    0.0,
                    Math.min(
                            1.0,
                            currentHeat
                                    / (double) finalUnlockHeat
                    )
            );

    int color =
            HeatTier.getColor(
                    currentHeat
            );

    if (filledWidth > 0) {
        graphics.fill(
                x,
                y,
                x + filledWidth,
                y + 2,
                HeatTier.getColor(
                        currentHeat
                )
        );

        /*
         * Tiny highlight gives it a little
         * more depth without becoming chunky.
         */
        // graphics.fill(
        //         x,
        //         y,
        //         x + filledWidth,
        //         y + 1,
        //         lightenColor(
        //                 color
        //         )
        // );
    }

    /*
     * Unknown continuation.
     */
    int unknownStart =
            x + knownWidth;

    int unknownWidth =
            width - knownWidth;

    for (
            int i = 0;
            i < unknownWidth;
            i += 4
    ) {
        graphics.fill(
                unknownStart + i,
                y + 1,
                Math.min(
                        unknownStart + i + 2,
                        x + width
                ),
                y + 2,
                0xFF555555
        );
    }
}

    private static double calculateDangerProgress(
                List<MobProgress> mobs,
                int currentHeat
        ) {
        if (mobs.isEmpty()) {
                return 0.0;
        }

        int finalUnlockHeat = mobs.stream()
                .mapToInt(MobProgress::unlockHeat)
                .max()
                .orElse(1);

        if (finalUnlockHeat <= 0) {
                return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        currentHeat / (double) finalUnlockHeat
                )
        );
        }

    private static void drawMobMarkers(
        GuiGraphics graphics,
        Minecraft minecraft,
        List<MobProgress> mobs,
        int currentHeat,
        int barX,
        int barY,
        int barWidth
) {
    int finalUnlockHeat =
            mobs.stream()
                    .mapToInt(
                            MobProgress::unlockHeat
                    )
                    .max()
                    .orElse(1);

    int knownWidth =
            Math.round(
                    barWidth * 0.82F
            );

    for (int i = 0; i < mobs.size(); i++) {

        MobProgress mob =
                mobs.get(i);

        double location =
                mob.unlockHeat()
                        / (double) finalUnlockHeat;

        int centerX =
                barX
                        + Math.round(
                                knownWidth
                                        * (float) location
                        );

        boolean unlocked =
                currentHeat
                        >= mob.unlockHeat();

        drawMobMarker(
                graphics,
                minecraft,
                mob,
                currentHeat,
                centerX,
                barY,
                unlocked,
                i
        );
        }
}

    private static void drawMobMarker(
        GuiGraphics graphics,
        Minecraft minecraft,
        MobProgress mob,
        int currentHeat,
        int centerX,
        int barY,
        boolean unlocked,
        int index
) {
    int iconSize = 16;

    int iconX =
            centerX
                    - iconSize / 2;

    int iconY =
            barY - 25;


    /*
     * Unlock tick.
     */
    graphics.fill(
            centerX,
            barY - 2,
            centerX + 1,
            barY + 6,
            unlocked
                    ? HeatTier.getColor(
                            currentHeat
                    )
                    : 0xFF666666
    );


    /*
     * Icon tint.
     */
    if (unlocked) {
        graphics.setColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
    else {
        graphics.setColor(
                0.22F,
                0.22F,
                0.22F,
                1.0F
        );
    }

    graphics.blit(
            mob.icon(),
            iconX,
            iconY,
            0,
            0,
            iconSize,
            iconSize,
            iconSize,
            iconSize
    );

    graphics.setColor(
            1.0F,
            1.0F,
            1.0F,
            1.0F
    );

    float statScale =
                0.72F;


    /*
     * Locked.
     */
    if (!unlocked) {

        String unlockText =
                "HEAT "
                        + mob.unlockHeat();

        boolean above =
                index % 2 != 0;

        float labelY =
                above
                        ? barY - 40
                        : barY + 10;

        drawScaledCenteredString(
                graphics,
                minecraft,
                unlockText,
                centerX,
                labelY,
                0xFF777777,
                0.72F
        );

        return;
        }


    /*
     * Unlocked stats.
     */
    double health =
            MobScaling.getEffectiveHealth(
                    mob.type(),
                    currentHeat
            );

    double damage =
            MobScaling.getEffectiveDamage(
                    mob.type(),
                    currentHeat
            );

        drawScaledCenteredString(
                graphics,
                minecraft,
                "HP " + formatStat(health),
                centerX,
                barY + 10,
                0xFFFF7777,
                statScale
        );

        drawScaledCenteredString(
                graphics,
                minecraft,
                "DMG " + formatStat(damage),
                centerX,
                barY + 18,
                0xFFFFCC66,
                statScale
        );
}

private static void drawCurrentHeatMarker(
        GuiGraphics graphics,
        Minecraft minecraft,
        List<MobProgress> mobs,
        int currentHeat,
        int barX,
        int barY,
        int barWidth
) {
    if (mobs.isEmpty()) {
        return;
    }

    int finalUnlockHeat =
            mobs.stream()
                    .mapToInt(
                            MobProgress::unlockHeat
                    )
                    .max()
                    .orElse(1);

    int knownWidth =
            Math.round(
                    barWidth * 0.82F
            );

    double progress =
            Math.max(
                    0.0,
                    Math.min(
                            1.0,
                            currentHeat
                                    / (double) finalUnlockHeat
                    )
            );

    int markerX =
            barX
                    + Math.round(
                    knownWidth
                            * (float) progress
            );

    int color =
            HeatTier.getColor(
                    currentHeat
            );

        /*
        * Current Heat pointer.
        */
        graphics.fill(
                markerX - 2,
                barY - 7,
                markerX + 3,
                barY - 6,
                color
        );

        graphics.fill(
                markerX - 1,
                barY - 6,
                markerX + 2,
                barY - 5,
                color
        );

        graphics.fill(
                markerX,
                barY - 5,
                markerX + 1,
                barY - 2,
                color
        );
}

private static void drawScaledCenteredString(
        GuiGraphics graphics,
        Minecraft minecraft,
        String text,
        float centerX,
        float y,
        int color,
        float scale
) {
    float textWidth =
            minecraft.font.width(text)
                    * scale;

    graphics.pose().pushPose();

    graphics.pose().translate(
            centerX - textWidth / 2.0F,
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

private static void drawUnknownFuture(
        GuiGraphics graphics,
        Minecraft minecraft,
        List<MobProgress> mobs,
        int barX,
        int barY,
        int barWidth
) {
    if (mobs.isEmpty()) {
        return;
    }

    int knownWidth =
            Math.round(
                    barWidth * 0.82F
            );

    int unknownStart =
            barX + knownWidth;

    int unknownEnd =
            barX + barWidth;

    int centerX =
            unknownStart
                    + (
                    unknownEnd
                            - unknownStart
            ) / 2;

    /*
     * Only one mysterious label.
     *
     * No "UNKNOWN" underneath it — that was
     * making this look like another mob marker.
     */
    graphics.drawCenteredString(
            minecraft.font,
            "???",
            centerX,
            barY - 24,
            0xFF777777
    );
}

    private static double getHeatBarPosition(
                int heat,
                int finalUnlockHeat
    ) {
        if (finalUnlockHeat <= 0) {
                return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        heat / (double) finalUnlockHeat
                )
        );
    }

    private static String formatStat(double value) {
        if (value == Math.floor(value)) {
            return Integer.toString((int) value);
        }

        return String.format("%.1f", value);
    }

    private static List<MobProgress> createMobProgressList() {
        return List.of(
                new MobProgress(
                        EntityType.ZOMBIE,
                        ZOMBIE_ICON,
                        TheFloodConfig.MOBS.zombie.unlockHeat.get()
                ),
                new MobProgress(
                        EntityType.SKELETON,
                        SKELETON_ICON,
                        TheFloodConfig.MOBS.skeleton.unlockHeat.get()
                ),
                new MobProgress(
                        EntityType.SPIDER,
                        SPIDER_ICON,
                        TheFloodConfig.MOBS.spider.unlockHeat.get()
                ),
                new MobProgress(
                        EntityType.CREEPER,
                        CREEPER_ICON,
                        TheFloodConfig.MOBS.creeper.unlockHeat.get()
                ),
                new MobProgress(
                        EntityType.ENDERMAN,
                        ENDERMAN_ICON,
                        TheFloodConfig.MOBS.enderman.unlockHeat.get()
                ),
                new MobProgress(
                        EntityType.WARDEN,
                        WARDEN_ICON,
                        TheFloodConfig.MOBS.warden.unlockHeat.get()
                )
        );
    }

    private record MobProgress(
        EntityType<?> type,
        ResourceLocation icon,
        int unlockHeat
    ) {
    }
}