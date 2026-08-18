package com.jushymaso222.theflood.team;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class TeamChatDisplayEvents {

    private TeamChatDisplayEvents() {
    }

    @SubscribeEvent
    public static void onServerChat(
            ServerChatEvent event
    ) {
        ServerPlayer player =
                event.getPlayer();

        FloodTeam team =
                TeamManager.getTeamForPlayer(
                        player
                );

        /*
        * No Flood team:
        * leave vanilla chat untouched.
        */
        if (team == null) {
            return;
        }

        TeamChatManager.ChatMode mode =
                TeamChatManager.getMode(
                        player.getUUID()
                );

        Component teamPrefix =
                Component.literal(
                        "["
                                + team.getName()
                                + "] "
                ).withStyle(
                        TeamDisplayManager.getChatColor(
                                team.getColor()
                        )
                );

        Component playerName =
                Component.literal(
                        player.getGameProfile()
                                .getName()
                ).withStyle(
                        ChatFormatting.WHITE
                );

        Component separator =
                Component.literal(
                        ": "
                ).withStyle(
                        ChatFormatting.WHITE
                );

        Component message =
                Component.literal(
                        event.getRawText()
                ).withStyle(
                        ChatFormatting.WHITE
                );

        Component formattedMessage =
                Component.empty()
                        .append(teamPrefix)
                        .append(playerName)
                        .append(separator)
                        .append(message);

        event.setCanceled(true);

        /*
        * TEAM CHAT
        */
        if (
                mode
                        == TeamChatManager.ChatMode.TEAM
        ) {
            Component teamChatMessage =
                    Component.literal(
                            "[TEAM] "
                    ).withStyle(
                            ChatFormatting.AQUA
                    )
                    .append(
                            formattedMessage
                    );

            for (UUID memberId :
                    team.getMembers()) {

                ServerPlayer member =
                        player.server
                                .getPlayerList()
                                .getPlayer(
                                        memberId
                                );

                if (member == null) {
                    continue;
                }

                member.sendSystemMessage(
                        teamChatMessage
                );
            }

            return;
        }

        /*
        * GLOBAL CHAT
        */
        player.server
                .getPlayerList()
                .broadcastSystemMessage(
                        formattedMessage,
                        false
                );
    }
}