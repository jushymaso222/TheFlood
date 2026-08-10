package com.jushymaso222.theflood.debug;

import com.jushymaso222.theflood.progression.PlayerFloodData;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.chat.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.jushymaso222.theflood.progression.HeatManager;

public final class DummyPlayerManager {

    private static final Map<String, FakePlayer> DUMMIES =
        new HashMap<>();

    private static final Map<String, ArmorStand> DUMMY_VISUALS =
            new HashMap<>();

    private static final Random RANDOM =
            new Random();

    private DummyPlayerManager() {
    }

    private static long lastVisualUpdate = -1;

    public static void tick(
            ServerLevel level
    ) {
        long gameTime =
                level.getGameTime();

        /*
        * Update twice per second instead of 20 times/sec.
        */
        if (
                lastVisualUpdate >= 0
                && gameTime - lastVisualUpdate < 10
        ) {
            return;
        }

        lastVisualUpdate =
                gameTime;

        for (Map.Entry<String, FakePlayer> entry :
                DUMMIES.entrySet()) {

            String key =
                    entry.getKey();

            FakePlayer dummy =
                    entry.getValue();

            if (dummy == null) {
                continue;
            }

            /*
            * Only process dummies belonging to this level.
            */
            if (dummy.serverLevel() != level) {
                continue;
            }

            ArmorStand visual =
                    DUMMY_VISUALS.get(
                            key
                    );

            if (
                    visual == null
                    || visual.isRemoved()
            ) {
                continue;
            }

            /*
            * Keep the armor stand at the FakePlayer's
            * logical position.
            */
            visual.moveTo(
                    dummy.getX(),
                    dummy.getY(),
                    dummy.getZ(),
                    dummy.getYRot(),
                    dummy.getXRot()
            );

            updateVisualName(
                    dummy,
                    visual
            );
        }
    }

    private static void updateVisualName(
            FakePlayer dummy,
            ArmorStand visual
    ) {
        int effectiveHeat =
                HeatManager.getEffectiveHeat(
                        dummy
                );

        visual.setCustomName(
                Component.literal(
                        dummy.getGameProfile()
                                .getName()
                                + " [Heat "
                                + effectiveHeat
                                + "]"
                )
        );

        visual.setCustomNameVisible(true);
    }

    public static FakePlayer addDummy(
            ServerPlayer creator,
            String name
    ) {
        String cleanName =
                name.trim();

        if (cleanName.isEmpty()) {
            return null;
        }

        String key =
                cleanName.toLowerCase();

        if (DUMMIES.containsKey(key)) {
            return null;
        }

        ServerLevel level =
                creator.serverLevel();

        /*
         * Stable UUID based on dummy name.
         *
         * This prevents the same named dummy from receiving
         * a completely different identity every time.
         */
        UUID uuid =
                UUID.nameUUIDFromBytes(
                        (
                                "theflood_dummy_"
                                        + key
                        ).getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        GameProfile profile =
                new GameProfile(
                        uuid,
                        cleanName
                );

        FakePlayer dummy =
                FakePlayerFactory.get(
                        level,
                        profile
                );

        dummy.moveTo(
                creator.getX(),
                creator.getY(),
                creator.getZ(),
                creator.getYRot(),
                creator.getXRot()
        );

        ArmorStand visual =
            new ArmorStand(
                level,
                creator.getX(),
                creator.getY(),
                creator.getZ()
        );

        updateVisualName(
                dummy,
                visual
        );

        visual.setNoGravity(true);

        visual.setInvulnerable(true);

        level.addFreshEntity(
                visual
        );

        DUMMY_VISUALS.put(
                key,
                visual
        );

        /*
         * Development dummies receive a random Solo Heat
         * from 1 through 100.
         */
        int randomHeat =
                1 + RANDOM.nextInt(
                        PlayerFloodData.MAX_HEAT
                );

        PlayerFloodData.setSoloHeat(
                dummy,
                randomHeat
        );

        /*
         * Reset partial Heat progression so the dummy starts
         * exactly at the randomly selected Heat level.
         */
        PlayerFloodData.setHeatProgressTicks(
                dummy,
                0
        );

        DUMMIES.put(
                key,
                dummy
        );

        return dummy;
    }

    public static boolean removeDummy(
            String name
    ) {
        String key =
                name.trim()
                        .toLowerCase();

        FakePlayer dummy =
                DUMMIES.remove(
                        key
                );

        ArmorStand visual =
                DUMMY_VISUALS.remove(
                        key
                );

        if (dummy == null) {
            return false;
        }

        if (visual != null) {
            visual.discard();
        }

        if (!dummy.isRemoved()) {
            dummy.discard();
        }

        return true;
    }

    public static boolean isDummy(
            ServerPlayer player
    ) {
        return player instanceof FakePlayer
                && DUMMIES.values()
                        .stream()
                        .anyMatch(
                                dummy ->
                                        dummy.getUUID()
                                                .equals(
                                                        player.getUUID()
                                                )
                        );
    }

    public static boolean isDummy(
            UUID playerId
    ) {
        return DUMMIES.values()
                .stream()
                .anyMatch(
                        dummy ->
                                dummy.getUUID()
                                        .equals(playerId)
                );
    }

    public static FakePlayer getDummy(
            String name
    ) {
        return DUMMIES.get(
                name.trim()
                        .toLowerCase()
        );
    }

    public static FakePlayer getDummy(
            UUID playerId
    ) {
        return DUMMIES.values()
                .stream()
                .filter(
                        dummy ->
                                dummy.getUUID()
                                        .equals(playerId)
                )
                .findFirst()
                .orElse(null);
    }

    public static Collection<FakePlayer> getDummies() {
        return Collections.unmodifiableCollection(
                DUMMIES.values()
        );
    }

    public static int getDummyHeat(
            String name
    ) {
        FakePlayer dummy =
                getDummy(name);

        if (dummy == null) {
            return 0;
        }

        return PlayerFloodData.getSoloHeat(
                dummy
        );
    }

    public static void clearAll() {

        for (ArmorStand visual :
                DUMMY_VISUALS.values()) {

            if (!visual.isRemoved()) {
                visual.discard();
            }
        }

        DUMMY_VISUALS.clear();

        for (FakePlayer dummy :
                DUMMIES.values()) {

            if (!dummy.isRemoved()) {
                dummy.discard();
            }
        }

        DUMMIES.clear();
    }
}