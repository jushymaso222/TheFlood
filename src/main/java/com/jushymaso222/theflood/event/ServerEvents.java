package com.jushymaso222.theflood.event;
import com.jushymaso222.theflood.spawn.SpawnDirector;
import com.jushymaso222.theflood.horde.HordeDirector;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ServerEvents {

    private static long lastAnnouncedBloodMoonDay = -1;
    private static int previousDay = -1;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel level)) {
            return;
        }

        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        long dayTime = level.getDayTime();
        long day = (dayTime / 24000L) + 1;
        long timeOfDay = dayTime % 24000L;

        int currentDay = (int) day;
        if (previousDay == -1) {
            // Initialize without announcing old unlocks when loading an existing world.
            previousDay = currentDay;
        } else if (currentDay != previousDay) {
            announceMobUnlocks(level, currentDay);
            previousDay = currentDay;
        }

        boolean isBloodMoonDay = day % TheFloodConfig.TIME.bloodMoonFrequencyDays.get() == 0;
        boolean justBecameNight = timeOfDay >= 13000L && timeOfDay <= 13100L;

        

        if (isBloodMoonDay && justBecameNight && lastAnnouncedBloodMoonDay != day) {
            lastAnnouncedBloodMoonDay = day;
            announceBloodMoon(level);
        }
        SpawnDirector.tick(level);
        HordeDirector.tick(level);
    }

    private static void announceMobUnlocks(ServerLevel level, int day) {
        if (day == TheFloodConfig.MOBS.zombie.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "The dead have begun to rise...",
                    "Zombies can now appear."
            );
        }

        if (day == TheFloodConfig.MOBS.skeleton.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "You hear bones rattling in the distance...",
                    "Skeletons can now appear."
            );
        }

        if (day == TheFloodConfig.MOBS.spider.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "Something crawls through the darkness...",
                    "Spiders can now appear."
            );
        }

        if (day == TheFloodConfig.MOBS.creeper.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "A faint hissing echoes across the land...",
                    "Creepers can now appear."
            );
        }

        if (day == TheFloodConfig.MOBS.enderman.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "The space between worlds has begun to weaken...",
                    "Endermen can now appear."
            );
        }

        if (day == TheFloodConfig.MOBS.warden.unlockDay.get()) {
            broadcastUnlockMessage(
                    level,
                    "Something ancient has awakened beneath the earth...",
                    "Flood Wardens can now appear."
            );
        }
    }

    private static void broadcastUnlockMessage(
            ServerLevel level,
            String warning,
            String unlock
    ) {
        Component warningMessage = Component.literal(warning)
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);

        Component unlockMessage = Component.literal(unlock)
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(Component.empty());
            player.sendSystemMessage(warningMessage);
            player.sendSystemMessage(unlockMessage);
            player.sendSystemMessage(Component.empty());
        }
    }

    private static void announceBloodMoon(ServerLevel level) {
        Component title = Component.literal("BLOOD MOON")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

        Component subtitle = Component.literal("The horde is coming...")
                .withStyle(ChatFormatting.RED);

        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }
}