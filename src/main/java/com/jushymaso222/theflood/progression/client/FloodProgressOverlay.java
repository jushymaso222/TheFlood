package com.jushymaso222.theflood.progression.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.jushymaso222.theflood.progression.scaling.MobScaling;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
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

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "flood_progress",
                FloodProgressOverlay::render
        );
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

        drawPanel(
                graphics,
                minecraft,
                mobs,
                currentHeat,
                panelX,
                panelY
        );
    }

    private static void drawPanel(
            GuiGraphics graphics,
            Minecraft minecraft,
            List<MobProgress> mobs,
            int currentHeat,
            int panelX,
            int panelY
    ) {
        int panelRight = panelX + PANEL_WIDTH;
        int panelBottom = panelY + PANEL_HEIGHT;

        // Semi-transparent black background.
        graphics.fill(
                panelX,
                panelY,
                panelRight,
                panelBottom,
                0xCC101010
        );

        // Border.
        graphics.fill(
                panelX,
                panelY,
                panelRight,
                panelY + 1,
                0xFF8A1C1C
        );

        graphics.fill(
                panelX,
                panelBottom - 1,
                panelRight,
                panelBottom,
                0xFF8A1C1C
        );

        graphics.fill(
                panelX,
                panelY,
                panelX + 1,
                panelBottom,
                0xFF8A1C1C
        );

        graphics.fill(
                panelRight - 1,
                panelY,
                panelRight,
                panelBottom,
                0xFF8A1C1C
        );

        String title = "THE FLOOD — HEAT " + currentHeat;

        graphics.drawCenteredString(
                minecraft.font,
                title,
                panelX + PANEL_WIDTH / 2,
                panelY + 7,
                0xFFFFFF
        );

        String dangerLabel = getDangerLabel(mobs, currentHeat);
        int dangerColor = getDangerColor(mobs, currentHeat);

        graphics.drawCenteredString(
                minecraft.font,
                dangerLabel,
                panelX + PANEL_WIDTH / 2,
                panelY + 20,
                dangerColor
        );

        int barX = panelX + 16;
        int barY = panelY + 55;
        int barWidth = PANEL_WIDTH - 32;

        drawProgressBar(
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
    }

    private static void drawProgressBar(
            GuiGraphics graphics,
            List<MobProgress> mobs,
            int currentHeat,
            int x,
            int y,
            int width
    ) {
        int barHeight = 7;

        // Outer border.
        graphics.fill(
                x - 1,
                y - 1,
                x + width + 1,
                y + barHeight + 1,
                0xFF111111
        );

        // Empty meter background.
        graphics.fill(
                x,
                y,
                x + width,
                y + barHeight,
                0xFF353535
        );

        double progress = calculateDangerProgress(mobs, currentHeat);
        int filledWidth = (int) Math.round(width * progress);

        int dangerColor = getDangerColor(mobs, currentHeat);

        if (filledWidth > 0) {
            graphics.fill(
                    x,
                    y,
                    x + filledWidth,
                    y + barHeight,
                    dangerColor
            );

            // Thin highlight along the top of the filled area.
            graphics.fill(
                    x,
                    y,
                    x + filledWidth,
                    y + 1,
                    lightenColor(dangerColor)
            );
        }

        // Add small divisions to make the meter feel staged.
        // for (int i = 1; i < mobs.size(); i++) {
        //     int dividerX = x + (int) Math.round(width * (i / (double) mobs.size()));

        //     graphics.fill(
        //             dividerX,
        //             y,
        //             dividerX + 1,
        //             y + barHeight,
        //             0xAA111111
        //     );
        // }
    }

    private static String getDangerLabel(
            List<MobProgress> mobs,
            int currentHeat
    ) {
        int unlocked = countUnlockedMobs(mobs, currentHeat);

        return switch (unlocked) {
            case 0 -> "DANGER: DORMANT";
            case 1 -> "DANGER: MINIMAL";
            case 2 -> "DANGER: RISING";
            case 3 -> "DANGER: DANGEROUS";
            case 4 -> "DANGER: SEVERE";
            case 5 -> "DANGER: CRITICAL";
            default -> "DANGER: THE FLOOD";
        };
    }

    private static int countUnlockedMobs(
            List<MobProgress> mobs,
            int currentHeat
    ) {
        int count = 0;

        for (MobProgress mob : mobs) {
            if (currentHeat >= mob.unlockHeat()) {
                count++;
            }
        }

        return count;
    }

    private static int getDangerColor(
            List<MobProgress> mobs,
            int currentHeat
    ) {
        int unlocked = countUnlockedMobs(mobs, currentHeat);

        return switch (unlocked) {
            case 0 -> 0xFF6B7770;
            case 1 -> 0xFF6B7770;
            case 2 -> 0xFF9D9A45;
            case 3 -> 0xFFD09038;
            case 4 -> 0xFFD45A32;
            case 5 -> 0xFFC92F2F;
            default -> 0xFF8F1515;
        };
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
        int finalUnlockDay = mobs.stream()
                .mapToInt(MobProgress::unlockHeat)
                .max()
                .orElse(1);

        for (MobProgress mob : mobs) {
            double location =
                    mob.unlockHeat() / (double) finalUnlockDay;

            int centerX =
                    barX + (int) Math.round(barWidth * location);

            boolean unlocked =
                    currentHeat >= mob.unlockHeat();

            if (mob.type == EntityType.WARDEN) {
                drawMobMarker(
                        graphics,
                        minecraft,
                        mob,
                        currentHeat,
                        centerX - 7,
                        barY,
                        unlocked
                );
            } else {
                drawMobMarker(
                        graphics,
                        minecraft,
                        mob,
                        currentHeat,
                        centerX,
                        barY,
                        unlocked
                );
            }
        }
    }

    private static void drawMobMarker(
            GuiGraphics graphics,
            Minecraft minecraft,
            MobProgress mob,
            int currentHeat,
            int centerX,
            int barY,
            boolean unlocked
    ) {
        int iconSize = 16;
        int iconX = centerX - iconSize / 2;
        int iconY = barY - 20;
        int textStartY = barY + 11;

        /*
         * Locked icons receive a dark gray tint.
         * Unlocked icons render normally.
         */
        if (unlocked) {
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            graphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
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

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!unlocked) {
            String unlockText = "H" + mob.unlockHeat();

            graphics.drawCenteredString(
                    minecraft.font,
                    unlockText,
                    centerX,
                    textStartY,
                    0x777777
            );

            return;
        }

        double health = MobScaling.getEffectiveHealth(
                mob.type(),
                currentHeat
        );

        double damage = MobScaling.getEffectiveDamage(
                mob.type(),
                currentHeat
        );

        String healthText =
                "HP " + formatStat(health);

        String damageText =
                "DMG " + formatStat(damage);

        graphics.drawCenteredString(
                minecraft.font,
                healthText,
                centerX,
                textStartY,
                0xFF7777
        );

        graphics.drawCenteredString(
                minecraft.font,
                damageText,
                centerX,
                textStartY + 9,
                0xFFCC66
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