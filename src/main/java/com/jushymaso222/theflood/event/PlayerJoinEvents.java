package com.jushymaso222.theflood.event;

import com.jushymaso222.theflood.TheFlood;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.ChatFormatting;

@Mod.EventBusSubscriber(
    modid = TheFlood.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class PlayerJoinEvents {
    private static final String PROGRESS_HINT_SHOWN = "theflood_progress_hint_shown";

    private PlayerJoinEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.getPersistentData().getBoolean(PROGRESS_HINT_SHOWN)) {
            return;
        }

        sendProgressHint(player);

        player.getPersistentData().putBoolean(PROGRESS_HINT_SHOWN, true);
    }

    private static void sendProgressHint(ServerPlayer player) {
        Component prefix = Component.literal("[The Flood] ")
            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        
        Component message = Component.literal("Hold TAB to view the current danger level, enemy unlocks, health, and damage.")
            .withStyle(ChatFormatting.GRAY);

        player.sendSystemMessage(
            Component.empty().append(prefix).append(message)
        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        boolean hintWasShown = event.getOriginal()
            .getPersistentData()
            .getBoolean(PROGRESS_HINT_SHOWN);
        
        if (hintWasShown) {
            event.getEntity()
                .getPersistentData()
                .putBoolean(PROGRESS_HINT_SHOWN, true);
        }
    }
}