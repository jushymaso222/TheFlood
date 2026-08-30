package com.jushymaso222.theflood.progression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.jushymaso222.theflood.config.TheFloodConfig;
import com.jushymaso222.theflood.debug.DummyPlayerManager;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.progression.network.SyncHeatPacket;
import com.jushymaso222.theflood.progression.network.FloodXpGainPacket;
import com.jushymaso222.theflood.team.FloodTeamSavedData;
import com.jushymaso222.theflood.team.FloodTeam;
import com.jushymaso222.theflood.team.TeamManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class HeatManager {

    private HeatManager() {
    }

    private static final String MOB_UNLOCK_PREFIX =
        "theflood_mob_unlock_";

    public static int getSoloHeat(
            ServerPlayer player
    ) {
        return PlayerFloodData.getSoloHeat(
                player
        );
    }

    private static void checkMobUnlockAnnouncements(
        ServerPlayer player,
        int effectiveHeat
) {

    checkMobUnlock(
            player,
            effectiveHeat,
            "zombie",
            TheFloodConfig.MOBS.zombie.unlockHeat.get(),
            "The dead have begun to rise...",
            "Zombies can now appear."
    );

    checkMobUnlock(
            player,
            effectiveHeat,
            "skeleton",
            TheFloodConfig.MOBS.skeleton.unlockHeat.get(),
            "You hear bones rattling in the distance...",
            "Skeletons can now appear."
    );

    checkMobUnlock(
            player,
            effectiveHeat,
            "spider",
            TheFloodConfig.MOBS.spider.unlockHeat.get(),
            "Something crawls through the darkness...",
            "Spiders can now appear."
    );

    checkMobUnlock(
            player,
            effectiveHeat,
            "creeper",
            TheFloodConfig.MOBS.creeper.unlockHeat.get(),
            "A faint hissing echoes across the land...",
            "Creepers can now appear."
    );

    checkMobUnlock(
            player,
            effectiveHeat,
            "enderman",
            TheFloodConfig.MOBS.enderman.unlockHeat.get(),
            "The space between worlds has begun to weaken...",
            "Endermen can now appear."
    );

    checkMobUnlock(
            player,
            effectiveHeat,
            "warden",
            TheFloodConfig.MOBS.warden.unlockHeat.get(),
            "Something ancient has awakened beneath the earth...",
            "Flood Wardens can now appear."
    );
}

private static void checkMobUnlock(
        ServerPlayer player,
        int effectiveHeat,
        String id,
        int unlockHeat,
        String warning,
        String unlock
) {

    if (effectiveHeat < unlockHeat) {
        return;
    }

    String key =
            MOB_UNLOCK_PREFIX
                    + id;

    /*
     * This specific player has already seen
     * this unlock announcement.
     */
    if (
            player.getPersistentData()
                    .getBoolean(key)
    ) {
        return;
    }

    player.getPersistentData()
            .putBoolean(
                    key,
                    true
            );

    Component warningMessage =
            Component.literal(
                    warning
            ).withStyle(
                    ChatFormatting.DARK_RED,
                    ChatFormatting.ITALIC
            );

    Component unlockMessage =
            Component.literal(
                    unlock
            ).withStyle(
                    ChatFormatting.RED,
                    ChatFormatting.BOLD
            );

    /*
     * Match the original ServerEvents layout exactly,
     * but only send it to this player.
     */
    player.sendSystemMessage(
            Component.empty()
    );

    player.sendSystemMessage(
            warningMessage
    );

    player.sendSystemMessage(
            unlockMessage
    );

    player.sendSystemMessage(
            Component.empty()
    );
}

    public static int getTeamHeat(
            ServerPlayer player
    ) {
        FloodTeam team =
                TeamManager.getTeamForPlayer(
                        player
                );

        if (team == null) {
            return 0;
        }

        return Math.max(
                1,
                team.getTeamHeat()
        );
    }

    public static boolean isInTeam(
            ServerPlayer player
    ) {
        return TeamManager.getTeamForPlayer(
                player
        ) != null;
    }

    public static int getBaseHeat(
                ServerPlayer player
    ) {
        FloodTeam team =
                TeamManager.getTeamForPlayer(player);

        if (team != null) {
                return PlayerFloodData.clampHeat(
                        team.getTeamHeat()
                );
        }

        return PlayerFloodData.clampHeat(
                getSoloHeat(player)
        );
    }

    public static int getProximityHeatBonus(
        ServerPlayer player
    ) {
        int radius =
                TheFloodConfig.HEAT
                        .proximityRadius
                        .get();

        double radiusSquared =
                radius * (double) radius;

        int nearbyPlayers = 0;

        /*
        * Combine real players and development dummies.
        * UUID keys prevent a FakePlayer from being counted twice.
        */
        Map<UUID, ServerPlayer> candidates =
                new LinkedHashMap<>();

        for (ServerPlayer realPlayer :
                player.serverLevel().players()) {

                candidates.put(
                        realPlayer.getUUID(),
                        realPlayer
                );
        }

        for (ServerPlayer dummy :
                DummyPlayerManager.getDummies()) {

                if (
                        dummy.serverLevel()
                                != player.serverLevel()
                ) {
                continue;
                }

                candidates.put(
                        dummy.getUUID(),
                        dummy
                );
        }

        for (ServerPlayer other :
                candidates.values()) {

                if (
                        other.getUUID()
                                .equals(player.getUUID())
                ) {
                continue;
                }

                if (!other.isAlive()) {
                continue;
                }

                if (other.isSpectator()) {
                continue;
                }

                if (
                        player.distanceToSqr(other)
                                > radiusSquared
                ) {
                continue;
                }

                /*
                * Members of the same formal team do not count here.
                * Their multiplayer scaling is already represented
                * by Team Heat.
                */
                if (areOnSameTeam(player, other)) {
                continue;
                }

                nearbyPlayers++;
        }

        if (nearbyPlayers <= 0) {
                return 0;
        }

        int basePerPlayer =
                TheFloodConfig.HEAT
                        .proximityBaseHeatPerPlayer
                        .get();

        int synergyPerPair =
                TheFloodConfig.HEAT
                        .proximitySynergyPerPlayer
                        .get();

        /*
        * Every nearby player contributes the normal base bonus.
        *
        * Default:
        * 1 player  = +3
        * 2 players = +6
        * 3 players = +9
        */
        int baseBonus =
                nearbyPlayers * basePerPlayer;

        /*
        * Add an extra nonlinear bonus based on how many
        * player pairs exist in the nearby group.
        *
        * 1 nearby player  -> 0 pairs
        * 2 nearby players -> 1 pair
        * 3 nearby players -> 3 pairs
        * 4 nearby players -> 6 pairs
        * 5 nearby players -> 10 pairs
        */
        int playerPairs =
                nearbyPlayers
                        * (nearbyPlayers - 1)
                        / 2;

        int synergyBonus =
                playerPairs * synergyPerPair;

        return baseBonus + synergyBonus;
    }

    public static void copyMobUnlockAnnouncementState(
                ServerPlayer original,
                ServerPlayer replacement
        ) {
        String[] mobIds = {
                "zombie",
                "skeleton",
                "spider",
                "creeper",
                "enderman",
                "warden"
        };

        for (String mobId : mobIds) {
                String key =
                        MOB_UNLOCK_PREFIX
                                + mobId;

                if (
                        original.getPersistentData()
                                .getBoolean(
                                        key
                                )
                ) {
                replacement.getPersistentData()
                        .putBoolean(
                                key,
                                true
                        );
                }
        }
        }

    private static boolean areOnSameTeam(
                ServerPlayer first,
                ServerPlayer second
    ) {
        FloodTeam firstTeam =
                TeamManager.getTeamForPlayer(first);

        FloodTeam secondTeam =
                TeamManager.getTeamForPlayer(second);

        if (
                firstTeam == null
                || secondTeam == null
        ) {
                return false;
        }

        return firstTeam.getTeamId()
                .equals(
                        secondTeam.getTeamId()
                );
    }

    public static int getEffectiveHeat(
                ServerPlayer player
    ) {
        int baseHeat =
                getBaseHeat(player);

        int proximityBonus =
                getProximityHeatBonus(player);

        return PlayerFloodData.clampHeat(
                baseHeat + proximityBonus
        );
    }

    public static long getFloodXp(
        ServerPlayer player
) {
    FloodTeam team =
            TeamManager.getTeamForPlayer(
                    player
            );

    if (team != null) {
        return team.getFloodXp();
    }

    return PlayerFloodData.getHeatProgressTicks(
            player
    );
}

public static long getFloodXpRequired(
        ServerPlayer player
) {
    FloodTeam team =
            TeamManager.getTeamForPlayer(
                    player
            );

    int heat =
            team != null
                    ? team.getTeamHeat()
                    : getSoloHeat(player);

    return getBaseFloodXpRequired(
            heat
    );
}

public static long getBaseFloodXpRequired(
        int heat
) {
    long baseXp =
            getTicksPerHeatLevel();

    int normalizedHeat =
            Math.max(
                    1,
                    heat
            );

    double growthPerHeat =
        TheFloodConfig.HEAT
                .floodXpGrowthPerHeat
                .get();

    double multiplier =
            1.0D
                    + (
                    (normalizedHeat - 1)
                            * growthPerHeat
            );

    return Math.max(
            1L,
            Math.round(
                    baseXp * multiplier
            )
    );
}

public static float getFloodXpProgress(
        ServerPlayer player
) {
    long required =
            getFloodXpRequired(player);

    if (required <= 0L) {
        return 0.0F;
    }

    return Math.max(
            0.0F,
            Math.min(
                    1.0F,
                    getFloodXp(player)
                            / (float) required
            )
    );
}

    public static long getTicksPerHeatLevel() {
        long dayMinutes =
                TheFloodConfig.TIME
                        .dayLengthMinutes
                        .get();

        long nightMinutes =
                TheFloodConfig.TIME
                        .nightLengthMinutes
                        .get();

        long totalMinutes =
                dayMinutes + nightMinutes;

        return totalMinutes
                * 60L
                * 20L;
    }

    public static boolean addFloodXp(
        ServerPlayer player,
        long amount
) {
    if (
            player == null
            || amount <= 0L
    ) {
        return false;
    }

    FloodTeam team =
            TeamManager.getTeamForPlayer(
                    player
            );

    /*
     * =====================================================
     * SOLO PROGRESSION
     * =====================================================
     */
    if (team == null) {
        return addSoloFloodXp(
                player,
                amount
        );
        }


    /*
     * =====================================================
     * TEAM PROGRESSION
     * =====================================================
     *
     * The team itself owns both:
     *
     * teamHeat
     * floodXp
     *
     * Individual Solo Heat is NOT touched here.
     */
    long progress =
            team.getFloodXp()
                    + amount;

    boolean leveledUp = false;

    while (
            team.getTeamHeat()
                    < PlayerFloodData.MAX_HEAT
    ) {
        long required =
                getBaseFloodXpRequired(
                        team.getTeamHeat()
                );

        if (progress < required) {
            break;
        }

        progress -= required;

        team.setTeamHeat(
                team.getTeamHeat() + 1
        );

        leveledUp = true;
    }

    if (
            team.getTeamHeat()
                    >= PlayerFloodData.MAX_HEAT
    ) {
        progress = 0L;
    }

    team.setFloodXp(
            progress
    );

    /*
     * Mark team data dirty for persistence.
     */
    FloodTeamSavedData
            .get(player.server)
            .setDirty();


    /*
     * Send the new shared Heat + XP state
     * to every online team member.
     */
    for (UUID memberId : team.getMembers()) {

        ServerPlayer member =
                player.server
                        .getPlayerList()
                        .getPlayer(
                                memberId
                        );

        if (member == null) {
            continue;
        }

        syncHeatToPlayer(
                member
        );

        /*
         * Also refresh the team HUD packet,
         * since ClientTeamData contains teamHeat.
         */
        TeamManager.syncTeamStateToPlayer(
                member
        );
    }

    return leveledUp;
}

public static boolean addRewardFloodXp(
        ServerPlayer player,
        long amount
) {
    if (
            player == null
            || amount <= 0L
    ) {
        return false;
    }

    boolean leveledUp =
            addFloodXp(
                    player,
                    amount
            );

    FloodNetwork.CHANNEL.send(
            PacketDistributor.PLAYER.with(
                    () -> player
            ),
            new FloodXpGainPacket(
                    amount
            )
    );

    return leveledUp;
}

    public static boolean advanceSoloHeat(
        ServerPlayer player
) {
    FloodTeam team =
            TeamManager.getTeamForPlayer(
                    player
            );

    /*
     * Solo player:
     * normal +1 passive progression.
     */
    if (team == null) {
        return addFloodXp(
                player,
                1L
        );
    }

    UUID authorityId = null;
    int onlineMembers = 0;

    /*
     * Find every real connected member of the team.
     *
     * One of them becomes the deterministic authority
     * responsible for applying this tick's shared XP.
     */
    for (UUID memberId : team.getMembers()) {

        ServerPlayer member =
                player.server
                        .getPlayerList()
                        .getPlayer(
                                memberId
                        );

        if (member == null) {
            continue;
        }

        /*
         * Development dummies should not cause a real
         * team's passive progression rate to increase.
         */
        if (DummyPlayerManager.isDummy(member)) {
            continue;
        }

        onlineMembers++;

        /*
         * Lowest UUID wins.
         *
         * This gives us a deterministic authority without
         * caring who owns the team.
         */
        if (
                authorityId == null
                || memberId.compareTo(authorityId) < 0
        ) {
            authorityId =
                    memberId;
        }
    }

    /*
     * Nobody online should be impossible here because
     * this method is being called from an online player,
     * but guard against it anyway.
     */
    if (
            authorityId == null
            || onlineMembers <= 0
    ) {
        return false;
    }

    /*
     * Only one member performs the shared progression
     * update this tick.
     */
    if (
            !player.getUUID()
                    .equals(authorityId)
    ) {
        return false;
    }

    /*
     * Scale passive progression by the number of members
     * who are actually online.
     *
     * 1 online -> +1
     * 2 online -> +2
     * 3 online -> +3
     * etc.
     */
    return addFloodXp(
            player,
            onlineMembers
    );
}

        public static boolean addSoloFloodXp(
        ServerPlayer player,
        long amount
) {
    if (
            player == null
            || amount <= 0L
    ) {
        return false;
    }

    long progress =
            PlayerFloodData.getHeatProgressTicks(
                    player
            ) + amount;

    boolean leveledUp = false;

    /*
     * This deliberately operates ONLY on the player's
     * personal Solo Heat.
     *
     * Team membership is completely ignored.
     */
    while (
            getSoloHeat(player)
                    < PlayerFloodData.MAX_HEAT
    ) {
        long required =
                getBaseFloodXpRequired(
                        getSoloHeat(player)
                );

        if (progress < required) {
            break;
        }

        progress -= required;

        PlayerFloodData.setSoloHeat(
                player,
                getSoloHeat(player) + 1
        );

        leveledUp = true;
    }

    if (
            getSoloHeat(player)
                    >= PlayerFloodData.MAX_HEAT
    ) {
        progress = 0L;
    }

    PlayerFloodData.setHeatProgressTicks(
            player,
            progress
    );

    /*
     * Still sync the player.
     *
     * If they're teamed, their effective/base Heat
     * remains the team's Heat, but their hidden personal
     * Solo progression is now up to date as well.
     */
    syncHeatToPlayer(
            player
    );

    return leveledUp;
}

    public static void syncHeatToPlayer(
                ServerPlayer player
    ) {
        if (DummyPlayerManager.isDummy(player)) {
                return;
        }

        int soloHeat =
                getSoloHeat(player);

        int teamHeat =
                getTeamHeat(player);

        int baseHeat =
                getBaseHeat(player);

        int proximityBonus =
                getProximityHeatBonus(player);

        long floodXp =
                getFloodXp(player);

        long floodXpRequired =
                getFloodXpRequired(player);

        int effectiveHeat =
                PlayerFloodData.clampHeat(
                        baseHeat + proximityBonus
                );

        checkMobUnlockAnnouncements(
                player,
                effectiveHeat
        );

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncHeatPacket(
                        soloHeat,
                        teamHeat,
                        baseHeat,
                        proximityBonus,
                        effectiveHeat,
                        floodXp,
                        floodXpRequired
                )
        );
    }
}