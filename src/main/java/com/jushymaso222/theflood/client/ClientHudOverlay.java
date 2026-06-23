package com.jushymaso222.theflood.client;

import com.jushymaso222.theflood.TheFlood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientHudOverlay {

    @SubscribeEvent
    public static void registerHud(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("flood_time_hud", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.level == null || mc.player == null) {
                return;
            }

            long dayTime = mc.level.getDayTime();
            long day = (dayTime / 24000L) + 1;
            long timeOfDay = dayTime % 24000L;

            String clockTime = formatMinecraftTime(timeOfDay);
            boolean bloodMoon = isBloodMoon(day, timeOfDay);

            String line1 = bloodMoon
                ? "BLOOD MOON - DAY " + day
                : "DAY " + day;

            String line2 = clockTime;

            int color = bloodMoon ? 0xFF3333 : 0xFFFFFF;

            int line1Width = mc.font.width(line1);
            int line2Width = mc.font.width(line2);

            int centerX = screenWidth / 2;

            guiGraphics.drawString(
                    mc.font,
                    line1,
                    centerX - (line1Width / 2),
                    8,
                    color,
                    true
            );

            guiGraphics.drawString(
                    mc.font,
                    line2,
                    centerX - (line2Width / 2),
                    20,
                    color,
                    true
            );
        });
    }

    private static String formatMinecraftTime(long timeOfDay) {
        long adjustedTime = (timeOfDay + 6000L) % 24000L;

        int hours = (int) (adjustedTime / 1000L);
        int minutes = (int) ((adjustedTime % 1000L) * 60L / 1000L);

        return String.format("%02d:%02d", hours, minutes);
    }

    private static boolean isBloodMoon(long day, long timeOfDay) {
        boolean isSeventhDay = day % 7 == 0;
        boolean isNight = timeOfDay >= 13000L && timeOfDay <= 23000L;

        return isSeventhDay && isNight;
    }
}