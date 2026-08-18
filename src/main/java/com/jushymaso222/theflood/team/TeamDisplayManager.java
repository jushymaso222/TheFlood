package com.jushymaso222.theflood.team;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public final class TeamDisplayManager {

    private static final String PREFIX =
            "flood_";

    private TeamDisplayManager() {
    }

    public static void syncPlayer(
            ServerPlayer player,
            FloodTeam floodTeam
    ) {
        MinecraftServer server =
                player.server;

        Scoreboard scoreboard =
                server.getScoreboard();

        String scoreboardName =
                getScoreboardTeamName(
                        floodTeam
                );

        /*
        * Compatibility guard.
        *
        * A player can only belong to one vanilla
        * scoreboard team at a time.
        *
        * If some other mod/server system already owns
        * that relationship, The Flood leaves it alone
        * instead of forcibly moving the player.
        */
        PlayerTeam currentTeam =
                scoreboard.getPlayersTeam(
                        player.getScoreboardName()
                );

        if (
                currentTeam != null
                && !currentTeam.getName()
                        .startsWith(PREFIX)
        ) {
            return;
        }

        PlayerTeam scoreboardTeam =
                scoreboard.getPlayerTeam(
                        scoreboardName
                );

        if (scoreboardTeam == null) {
            scoreboardTeam =
                    scoreboard.addPlayerTeam(
                            scoreboardName
                    );
        }

        /*
        * Cosmetic formatting.
        */
        scoreboardTeam.setPlayerPrefix(
                Component.literal(
                        "["
                                + floodTeam.getName()
                                + "] "
                ).withStyle(
                        getChatColor(
                                floodTeam.getColor()
                        )
                )
        );

        /*
        * Keep the actual username white.
        *
        * The prefix itself already carries the Flood
        * team's selected color.
        */
        scoreboardTeam.setColor(
                ChatFormatting.WHITE
        );

        /*
        * IMPORTANT:
        * Make the vanilla scoreboard team as close to
        * cosmetic-only as possible.
        *
        * FloodTeam/TeamManager remain the authoritative
        * gameplay team system.
        */
        scoreboardTeam.setAllowFriendlyFire(
                true
        );

        scoreboardTeam.setSeeFriendlyInvisibles(
                false
        );

        scoreboardTeam.setCollisionRule(
                Team.CollisionRule.ALWAYS
        );

        scoreboardTeam.setNameTagVisibility(
                Team.Visibility.ALWAYS
        );

        scoreboardTeam.setDeathMessageVisibility(
                Team.Visibility.ALWAYS
        );

        scoreboard.addPlayerToTeam(
                player.getScoreboardName(),
                scoreboardTeam
        );
    }

    public static void removePlayer(
            ServerPlayer player
    ) {
        Scoreboard scoreboard =
                player.server
                        .getScoreboard();

        PlayerTeam current =
                scoreboard.getPlayersTeam(
                        player.getScoreboardName()
                );

        /*
        * Never touch another mod's scoreboard team.
        */
        if (
                current == null
                || !current.getName()
                        .startsWith(PREFIX)
        ) {
            return;
        }

        scoreboard.removePlayerFromTeam(
                player.getScoreboardName(),
                current
        );
    }

    public static void removeTeam(
            MinecraftServer server,
            FloodTeam floodTeam
    ) {
        Scoreboard scoreboard =
                server.getScoreboard();

        PlayerTeam scoreboardTeam =
                scoreboard.getPlayerTeam(
                        getScoreboardTeamName(
                                floodTeam
                        )
                );

        if (scoreboardTeam != null) {
            scoreboard.removePlayerTeam(
                    scoreboardTeam
            );
        }
    }

    private static String getScoreboardTeamName(
            FloodTeam team
    ) {
        /*
         * Vanilla scoreboard team names must stay short,
         * so use part of the UUID instead of the actual
         * player-facing team name.
         */
        String raw =
                team.getTeamId()
                        .toString()
                        .replace("-", "");

        return PREFIX
                + raw.substring(
                        0,
                        10
                );
    }

    public static ChatFormatting getChatColor(
            DyeColor color
    ) {
        return switch (color) {
            case WHITE -> ChatFormatting.WHITE;
            case ORANGE -> ChatFormatting.GOLD;
            case MAGENTA -> ChatFormatting.LIGHT_PURPLE;
            case LIGHT_BLUE -> ChatFormatting.AQUA;
            case YELLOW -> ChatFormatting.YELLOW;
            case LIME -> ChatFormatting.GREEN;
            case PINK -> ChatFormatting.LIGHT_PURPLE;
            case GRAY -> ChatFormatting.DARK_GRAY;
            case LIGHT_GRAY -> ChatFormatting.GRAY;
            case CYAN -> ChatFormatting.DARK_AQUA;
            case PURPLE -> ChatFormatting.DARK_PURPLE;
            case BLUE -> ChatFormatting.BLUE;
            case BROWN -> ChatFormatting.GOLD;
            case GREEN -> ChatFormatting.DARK_GREEN;
            case RED -> ChatFormatting.RED;
            case BLACK -> ChatFormatting.BLACK;
        };
    }
}