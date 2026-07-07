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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnDirector {

    private static final Random RANDOM = new Random();

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) return;

        int day = getDay(level);
        if (day < TheFloodConfig.ZOMBIE_UNLOCK_DAY.get()) return;

        int baseAttempts = TheFloodConfig.BASE_SPAWN_ATTEMPTS.get();
        double attemptsPerDay = TheFloodConfig.SPAWN_ATTEMPTS_PER_DAY.get();
        int attempts = baseAttempts + (int) Math.floor((day - 1) * attemptsPerDay);

        int spawnRoll = TheFloodConfig.SPAWN_ROLL_CHANCE.get();

        for (ServerPlayer player : level.players()) {
            for (int i = 0; i < attempts; i++) {
                if (RANDOM.nextInt(spawnRoll) == 0) {
                    trySpawnNearPlayer(level, player, day, false, false);
                }
            }
        }
    }

    public static void trySpawnNearPlayer(
            ServerLevel level,
            ServerPlayer player,
            int day,
            boolean isHordeMob,
            boolean isBloodMoonMob
    ) {
        int mobCap = isBloodMoon(level)
                ? TheFloodConfig.BLOOD_MOON_MOB_CAP_PER_PLAYER.get()
                : TheFloodConfig.HOSTILE_MOB_CAP_PER_PLAYER.get();

        if (mobCap <= 0 || countHostileMobsNearPlayer(level, player) >= mobCap) {
            return;
        }

        if (getSpawnPool(day).isEmpty()) return;

        BlockPos pos = findSpawnPositionNearPlayer(level, player);
        if (pos == null) return;

        spawnMobAt(level, player, day, pos, isHordeMob, isBloodMoonMob);
    }

    public static BlockPos findSpawnPositionNearPlayer(ServerLevel level, ServerPlayer player) {
        int minDistance = TheFloodConfig.MIN_SPAWN_DISTANCE_FROM_PLAYER.get();
        int maxDistance = TheFloodConfig.MAX_SPAWN_DISTANCE_FROM_PLAYER.get();

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

            if (isValidSpawnPos(level, pos)) {
                return pos;
            }
        }

        return null;
    }

    public static void trySpawnNearPosition(
            ServerLevel level,
            ServerPlayer targetPlayer,
            int day,
            BlockPos center,
            int radius,
            boolean isHordeMob,
            boolean isBloodMoonMob
    ) {
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

            if (!isValidSpawnPos(level, pos)) continue;

            spawnMobAt(level, targetPlayer, day, pos, isHordeMob, isBloodMoonMob);
            return;
        }
    }

    private static void nerfFloodWarden(Mob mob) {
        double health = TheFloodConfig.FLOOD_WARDEN_HEALTH.get();
        double damage = TheFloodConfig.FLOOD_WARDEN_DAMAGE.get();
        double speed = TheFloodConfig.FLOOD_WARDEN_SPEED.get();

        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            mob.setHealth((float) health);
        }

        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        }

        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        }
    }

    private static void spawnMobAt(
            ServerLevel level,
            ServerPlayer player,
            int day,
            BlockPos pos,
            boolean isHordeMob,
            boolean isBloodMoonMob
    ) {
        List<SpawnEntry> pool = getSpawnPool(day);
        if (pool.isEmpty()) return;

        SpawnEntry entry = chooseWeighted(pool);

        Mob mob = entry.type.create(level);
        if (mob == null) return;

        mob.moveTo(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                RANDOM.nextFloat() * 360F,
                0
        );

        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(pos),
                MobSpawnType.EVENT,
                null,
                null
        );

        if (isHordeMob) {
            makeHordeMobAggressive(mob, player, isBloodMoonMob);
        }
        if (mob.getType() == EntityType.WARDEN) {
            nerfFloodWarden(mob);
        }

        level.addFreshEntityWithPassengers(mob);
    }

    private static void makeHordeMobAggressive(Mob mob, ServerPlayer player, boolean isBloodMoonMob) {
        double followRange = isBloodMoonMob
                ? TheFloodConfig.BLOOD_MOON_FOLLOW_RANGE.get()
                : TheFloodConfig.HORDE_FOLLOW_RANGE.get();

        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(followRange);
        }

        mob.setTarget(player);
        mob.setPersistenceRequired();
    }

    private static int countHostileMobsNearPlayer(ServerLevel level, ServerPlayer player) {
        int radius = TheFloodConfig.MAX_SPAWN_DISTANCE_FROM_PLAYER.get();
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

        return true;
    }

    private static boolean isBloodMoon(ServerLevel level) {
        int day = getDay(level);
        long timeOfDay = level.getDayTime() % 24000L;

        return day % TheFloodConfig.BLOOD_MOON_FREQUENCY_DAYS.get() == 0
                && timeOfDay >= 13000L
                && timeOfDay <= 23000L;
    }

    private static int getDay(ServerLevel level) {
        return (int) (level.getDayTime() / 24000L) + 1;
    }

    private static List<SpawnEntry> getSpawnPool(int day) {
        List<SpawnEntry> pool = new ArrayList<>();

        addMobIfUnlocked(pool, EntityType.ZOMBIE, day, TheFloodConfig.ZOMBIE_UNLOCK_DAY.get(), 10);
        addMobIfUnlocked(pool, EntityType.SKELETON, day, TheFloodConfig.SKELETON_UNLOCK_DAY.get(), 8);
        addMobIfUnlocked(pool, EntityType.SPIDER, day, TheFloodConfig.SPIDER_UNLOCK_DAY.get(), 8);
        addMobIfUnlocked(pool, EntityType.CREEPER, day, TheFloodConfig.CREEPER_UNLOCK_DAY.get(), 5);
        addMobIfUnlocked(pool, EntityType.ENDERMAN, day, TheFloodConfig.ENDERMAN_UNLOCK_DAY.get(), 4);
        addMobIfUnlocked(pool, EntityType.WARDEN, day, TheFloodConfig.WARDEN_UNLOCK_DAY.get(), 1);

        applyNewMobDip(pool);

        return pool;
    }

    private static void addMobIfUnlocked(
            List<SpawnEntry> pool,
            EntityType<? extends Mob> type,
            int currentDay,
            int unlockDay,
            int baseWeight
    ) {
        if (currentDay < unlockDay) return;

        double scaling = TheFloodConfig.SPAWN_SCALING_FACTOR.get();
        int daysUnlocked = currentDay - unlockDay;
        int weight = baseWeight + (int) Math.floor(daysUnlocked * scaling);

        pool.add(new SpawnEntry(type, Math.max(1, weight), unlockDay));
    }

    private static void applyNewMobDip(List<SpawnEntry> pool) {
        if (pool.size() <= 1) return;

        int newestUnlockDay = 0;

        for (SpawnEntry entry : pool) {
            newestUnlockDay = Math.max(newestUnlockDay, entry.unlockDay);
        }

        for (SpawnEntry entry : pool) {
            if (entry.unlockDay < newestUnlockDay) {
                entry.weight = Math.max(1, (int) (entry.weight * 0.75));
            }
        }
    }

    private static SpawnEntry chooseWeighted(List<SpawnEntry> pool) {
        int totalWeight = 0;

        for (SpawnEntry entry : pool) {
            totalWeight += entry.weight;
        }

        int roll = RANDOM.nextInt(totalWeight);

        for (SpawnEntry entry : pool) {
            roll -= entry.weight;

            if (roll < 0) {
                return entry;
            }
        }

        return pool.get(0);
    }

    private static class SpawnEntry {
        EntityType<? extends Mob> type;
        int weight;
        int unlockDay;

        SpawnEntry(EntityType<? extends Mob> type, int weight, int unlockDay) {
            this.type = type;
            this.weight = weight;
            this.unlockDay = unlockDay;
        }
    }
}