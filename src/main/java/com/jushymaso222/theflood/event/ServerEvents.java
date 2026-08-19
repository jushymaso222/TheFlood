package com.jushymaso222.theflood.event;
import com.jushymaso222.theflood.spawning.SpawnDirector;
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
import com.jushymaso222.theflood.debug.DummyPlayerManager;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ServerEvents {

    private static long lastAnnouncedBloodMoonDay = -1;

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

        boolean isBloodMoonDay = day % TheFloodConfig.TIME.bloodMoonFrequencyDays.get() == 0;
        boolean justBecameNight = timeOfDay >= 13000L && timeOfDay <= 13100L;

        

        if (isBloodMoonDay && justBecameNight && lastAnnouncedBloodMoonDay != day) {
            lastAnnouncedBloodMoonDay = day;
            announceBloodMoon(level);
        }
        SpawnDirector.tick(level);
        HordeDirector.tick(level);
        DummyPlayerManager.tick(level);
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