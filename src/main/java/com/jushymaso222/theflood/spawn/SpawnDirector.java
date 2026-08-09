package com.jushymaso222.theflood.spawn;

import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.monster.warden.Warden;
import com.jushymaso222.theflood.progression.HeatManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnDirector {

    private static final Random RANDOM = new Random();
    private static final Map<UUID, Long> NEXT_AMBIENT_REFILL_TIME =
        new HashMap<>();

    public static final String FLOOD_CONTROLLED_TAG = "theflood_controlled";
    private static final String FLOOD_SPAWN_KIND_TAG = "theflood_spawn_kind";

    private static final String SPAWN_KIND_AMBIENT = "ambient";
    private static final String SPAWN_KIND_MINI_HORDE = "mini_horde";
    private static final String SPAWN_KIND_BLOOD_MOON = "blood_moon"; 

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        // Blood Moon occurrence is still based on world day.
        if (isBloodMoon(level)) {
            return;
        }

        long gameTime = level.getGameTime();

        for (ServerPlayer player : level.players()) {
            int heat = HeatManager.getEffectiveHeat(player);

            if (heat < TheFloodConfig.MOBS.zombie.unlockHeat.get()) {
                continue;
            }

            int targetPopulation =
                    calculateAmbientPopulationTarget(
                            level,
                            player,
                            heat
                    );

            int currentPopulation =
                    countFloodMobsNearPlayer(
                            level,
                            player
                    );

            /*
            * Existing mini-horde and Blood Moon mobs count toward
            * current pressure. This prevents the ambient system from
            * replacing enemies while the player is already dealing
            * with an event.
            */
            if (currentPopulation >= targetPopulation) {
                continue;
            }

            UUID playerId =
                    player.getUUID();

            long nextRefillTime =
                    NEXT_AMBIENT_REFILL_TIME.getOrDefault(
                            playerId,
                            gameTime
                    );

            if (gameTime < nextRefillTime) {
                continue;
            }

            int deficit = targetPopulation - currentPopulation;

            if (deficit <= 0) {
                continue;
            }

            /*
            * Spawn more than one mob when the local Flood population
            * is far below its Heat-based target.
            */
            int spawnBatchSize;

            if (deficit >= 40) {
                spawnBatchSize = 8;
            } else if (deficit >= 30) {
                spawnBatchSize = 6;
            } else if (deficit >= 20) {
                spawnBatchSize = 5;
            } else if (deficit >= 12) {
                spawnBatchSize = 4;
            } else if (deficit >= 6) {
                spawnBatchSize = 2;
            } else {
                spawnBatchSize = 1;
            }

            spawnBatchSize =
                    Math.min(
                            spawnBatchSize,
                            deficit
                    );

            for (int i = 0; i < spawnBatchSize; i++) {
                trySpawnNearPlayer(
                        level,
                        player,
                        heat,
                        false,
                        false
                );
            }

            NEXT_AMBIENT_REFILL_TIME.put(
                    playerId,
                    gameTime
                            + calculateNormalSpawnCooldownTicks(
                                    level,
                                    player,
                                    heat
                            )
            );

            NEXT_AMBIENT_REFILL_TIME.put(
                    playerId,
                    gameTime
                            + calculateAmbientRefillDelayTicks()
            );
        }

        removeOfflinePlayerTimers(level);
    }

    public static int countFloodMobsNearPlayer(
            ServerLevel level,
            ServerPlayer player
    ) {
        int radius =
                TheFloodConfig.SPAWNING.maxSpawnDistanceFromPlayer.get();

        AABB area = player.getBoundingBox().inflate(radius);

        return level.getEntitiesOfClass(
                Monster.class,
                area,
                mob -> mob.isAlive()
                        && !mob.isRemoved()
                        && mob.getPersistentData()
                                .getBoolean(FLOOD_CONTROLLED_TAG)
        ).size();
    }

    private static long calculateAmbientRefillDelayTicks() {
        int minimumSeconds =
                TheFloodConfig.SPAWNING.ambientRefillMinSeconds.get();

        int maximumSeconds =
                TheFloodConfig.SPAWNING.ambientRefillMaxSeconds.get();

        if (maximumSeconds < minimumSeconds) {
            maximumSeconds = minimumSeconds;
        }

        int selectedSeconds = minimumSeconds;

        if (maximumSeconds > minimumSeconds) {
            selectedSeconds += RANDOM.nextInt(
                    maximumSeconds - minimumSeconds + 1
            );
        }

        return selectedSeconds * 20L;
    }

    private static int calculateAmbientPopulationTarget(
            ServerLevel level,
            ServerPlayer player,
            int heat
    ) {
        int unlockHeat =
                TheFloodConfig.MOBS
                        .zombie
                        .unlockHeat
                        .get();

        if (heat < unlockHeat) {
            return 0;
        }

        int startingPopulation =
                TheFloodConfig.SPAWNING
                        .startingAmbientPopulation
                        .get();

        int maximumPopulation =
                TheFloodConfig.SPAWNING
                        .maximumAmbientPopulation
                        .get();

        int hardCap =
                TheFloodConfig.SPAWNING
                        .hostileMobCapPerPlayer
                        .get();

        /*
        * Heat 100 is considered maximum progression.
        */
        int maximumHeat = 100;

        double progress =
                (heat - unlockHeat)
                        / (double) (maximumHeat - unlockHeat);

        progress =
                Math.max(
                        0.0,
                        Math.min(1.0, progress)
                );

        /*
        * A power greater than 1 keeps early progression gentle,
        * but causes population pressure to accelerate heavily
        * during late-game Heat levels.
        */
        double curvedProgress =
                Math.pow(progress, 1.7);

        int targetPopulation =
                startingPopulation
                        + (int) Math.round(
                                (maximumPopulation - startingPopulation)
                                        * curvedProgress
                        );

        targetPopulation =
                Math.min(
                        targetPopulation,
                        hardCap
                );

        if (isPlayerUnderground(level, player)) {
            double undergroundMultiplier =
                    TheFloodConfig.SPAWNING
                            .undergroundPopulationMultiplier
                            .get();

            targetPopulation =
                    (int) Math.floor(
                            targetPopulation
                                    * undergroundMultiplier
                    );
        }

        return Math.max(
                0,
                targetPopulation
        );
    }

    private static long calculateNormalSpawnCooldownTicks(
            ServerLevel level,
            ServerPlayer player,
            int heat
    ) {
        int firstHostileHeat =
                TheFloodConfig.MOBS
                        .zombie
                        .unlockHeat
                        .get();

        int scalingHeat =
                TheFloodConfig.SPAWNING
                        .spawnCooldownScalingDays
                        .get();

        int heatSinceHostilesStarted =
                Math.max(
                        0,
                        heat - firstHostileHeat
                );

        double progression =
                Math.min(
                        1.0,
                        heatSinceHostilesStarted
                                / (double) scalingHeat
                );

        int earlyMin =
                TheFloodConfig.SPAWNING
                        .earlyGameSpawnCooldownMinSeconds
                        .get();

        int earlyMax =
                TheFloodConfig.SPAWNING
                        .earlyGameSpawnCooldownMaxSeconds
                        .get();

        int lateMin =
                TheFloodConfig.SPAWNING
                        .lateGameSpawnCooldownMinSeconds
                        .get();

        int lateMax =
                TheFloodConfig.SPAWNING
                        .lateGameSpawnCooldownMaxSeconds
                        .get();

        double minimumSeconds =
                lerp(
                        earlyMin,
                        lateMin,
                        progression
                );

        double maximumSeconds =
                lerp(
                        earlyMax,
                        lateMax,
                        progression
                );

        double selectedSeconds =
                minimumSeconds
                        + RANDOM.nextDouble()
                        * Math.max(
                                0.0,
                                maximumSeconds - minimumSeconds
                        );

        if (isPlayerUnderground(level, player)) {
            selectedSeconds *=
                    TheFloodConfig.SPAWNING
                            .undergroundSpawnCooldownMultiplier
                            .get();
        }

        return Math.max(
                20L,
                Math.round(
                        selectedSeconds * 20.0
                )
        );
    }

    private static double lerp(
                double start,
                double end,
                double progress
        ) {
            return start + ((end - start) * progress);
        }

    private static boolean isPlayerUnderground(
            ServerLevel level,
            ServerPlayer player
    ) {
        BlockPos playerPos = player.blockPosition();

        int surfaceY = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                playerPos
        ).getY();

        /*
        * Requiring a meaningful gap avoids treating a player beneath
        * a small roof or tree as underground.
        */
        return surfaceY - playerPos.getY() >= 12;
    }

    private static void removeOfflinePlayerTimers(ServerLevel level) {
        NEXT_AMBIENT_REFILL_TIME.keySet().removeIf(
                uuid -> level.getServer()
                        .getPlayerList()
                        .getPlayer(uuid) == null
        );
    }

    public static void trySpawnNearPlayer(
            ServerLevel level,
            ServerPlayer player,
            int heat,
            boolean isHordeMob,
            boolean isBloodMoonMob
    ) {
        int mobCap =
                TheFloodConfig.SPAWNING
                        .hostileMobCapPerPlayer
                        .get();

        if (mobCap <= 0) {
            return;
        }

        if (
                countFloodMobsNearPlayer(
                        level,
                        player
                ) >= mobCap
        ) {
            return;
        }

        /*
        * Spawn eligibility is now based on the player's
        * effective Heat, not the world's current day.
        */
        if (getSpawnPool(heat).isEmpty()) {
            return;
        }

        BlockPos pos =
                findSpawnPositionNearPlayer(
                        level,
                        player
                );

        if (pos == null) {
            return;
        }

        spawnMobAt(
                level,
                player,
                heat,
                pos,
                isHordeMob,
                isBloodMoonMob
        );
    }

    public static BlockPos findSpawnPositionNearPlayer(ServerLevel level, ServerPlayer player) {
        int minDistance = TheFloodConfig.SPAWNING.minSpawnDistanceFromPlayer.get();
        int maxDistance = TheFloodConfig.SPAWNING.maxSpawnDistanceFromPlayer.get();

        if (maxDistance <= minDistance) {
            maxDistance = minDistance + 1;
        }

        for (int attempt = 0; attempt < 24; attempt++) {
            int distance = minDistance + RANDOM.nextInt(maxDistance - minDistance);
            double angle = RANDOM.nextDouble() * Math.PI * 2;

            int x = player.blockPosition().getX() + (int) (Math.cos(angle) * distance);
            int z = player.blockPosition().getZ() + (int) (Math.sin(angle) * distance);

            BlockPos pos = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, player.blockPosition().getY(), z)
            );

            int maximumVerticalDistance =
                    TheFloodConfig.SPAWNING.maximumVerticalSpawnDistance.get();

            int verticalDistance = Math.abs(
                    pos.getY() - player.blockPosition().getY()
            );

            if (verticalDistance > maximumVerticalDistance) {
                continue;
            }

            if (isValidSpawnPos(level, pos)) {
                return pos;
            }
        }

        return null;
    }

    public static boolean trySpawnNearPosition(
            ServerLevel level,
            ServerPlayer targetPlayer,
            int heat,
            BlockPos center,
            int radius,
            boolean isHordeMob,
            boolean isBloodMoonMob,
            int activeCap
    ) {
        if (activeCap <= 0) {
            return false;
        }

        if (
                countFloodMobsNearPlayer(
                        level,
                        targetPlayer
                ) >= activeCap
        ) {
            return false;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos pos = center.offset(
                    RANDOM.nextInt(radius * 2 + 1) - radius,
                    0,
                    RANDOM.nextInt(radius * 2 + 1) - radius
            );

            pos = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    pos
            );

            if (!isValidSpawnPos(level, pos)) {
                continue;
            }

            spawnMobAt(
                    level,
                    targetPlayer,
                    heat,
                    pos,
                    isHordeMob,
                    isBloodMoonMob
            );

            return true;
        }

        return false;
    }

    private static void spawnMobAt(
            ServerLevel level,
            ServerPlayer targetPlayer,
            int heat,
            BlockPos pos,
            boolean isHordeMob,
            boolean isBloodMoonMob
    ) {
        List<EntityType<? extends Mob>> spawnPool =
        getSpawnPool(heat);

        if (spawnPool.isEmpty()) {
            return;
        }

        EntityType<? extends Mob> selectedType =
                spawnPool.get(
                        RANDOM.nextInt(spawnPool.size())
                );

        Mob mob = selectedType.create(level);

        if (mob == null) {
            return;
        }

        mob.moveTo(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                RANDOM.nextFloat() * 360.0F,
                0.0F
        );

        if (
                isHordeMob
                || isBloodMoonMob
        ) {
            makeHordeMobAggressive(
                    mob,
                    targetPlayer,
                    isBloodMoonMob
            );
        }

        if (mob instanceof Warden warden) {
            warden.setPersistenceRequired();

            warden.increaseAngerAt(
                    targetPlayer,
                    100,
                    true
            );

            warden.setTarget(targetPlayer);
        }

        mob.getPersistentData().putBoolean(
                FLOOD_CONTROLLED_TAG,
                true
        );

        level.addFreshEntity(mob);
    }

    private static void makeHordeMobAggressive(Mob mob, ServerPlayer player, boolean isBloodMoonMob) {
        double followRange = isBloodMoonMob
                ? TheFloodConfig.HORDES.bloodMoonFollowRange.get()
                : TheFloodConfig.HORDES.miniHordeFollowRange.get();

        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(followRange);
        }

        mob.setTarget(player);
        mob.setPersistenceRequired();
    }

    private static int countHostileMobsNearPlayer(ServerLevel level, ServerPlayer player) {
        int radius = TheFloodConfig.SPAWNING.maxSpawnDistanceFromPlayer.get();
        AABB area = player.getBoundingBox().inflate(radius);

        return level.getEntitiesOfClass(
                Monster.class,
                area,
                mob -> mob.isAlive() && !mob.isRemoved()
        ).size();
    }

    private static boolean isValidSpawnPos(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isSolid()) return false;
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.above()).isAir()) return false;
        if (level.getBlockState(pos.below()).is(Blocks.BEDROCK)) return false;

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int maximumBlockLight = TheFloodConfig.SPAWNING.maximumSpawnBlockLight.get();
        if (blockLight > maximumBlockLight) {
            return false;
        }

        return true;
    }

    private static boolean isBloodMoon(ServerLevel level) {
        int day = getDay(level);
        long timeOfDay = level.getDayTime() % 24000L;

        return day % TheFloodConfig.TIME.bloodMoonFrequencyDays.get() == 0
                && timeOfDay >= 13000L
                && timeOfDay <= 23000L;
    }

    private static int getDay(ServerLevel level) {
        return (int) (level.getDayTime() / 24000L) + 1;
    }

    private static List<EntityType<? extends Mob>> getSpawnPool(
            int heat
    ) {
        List<EntityType<? extends Mob>> pool =
                new ArrayList<>();

        if (
                heat >= TheFloodConfig.MOBS
                        .zombie
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.ZOMBIE);
        }

        if (
                heat >= TheFloodConfig.MOBS
                        .skeleton
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.SKELETON);
        }

        if (
                heat >= TheFloodConfig.MOBS
                        .spider
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.SPIDER);
        }

        if (
                heat >= TheFloodConfig.MOBS
                        .creeper
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.CREEPER);
        }

        if (
                heat >= TheFloodConfig.MOBS
                        .enderman
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.ENDERMAN);
        }

        if (
                heat >= TheFloodConfig.MOBS
                        .warden
                        .unlockHeat
                        .get()
        ) {
            pool.add(EntityType.WARDEN);
        }

        return pool;
    }
}