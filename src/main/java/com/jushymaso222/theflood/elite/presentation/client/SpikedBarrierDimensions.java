package com.jushymaso222.theflood.elite.presentation.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;

public final class SpikedBarrierDimensions {

    private static final Map<EntityType<?>, Dimensions> DIMENSIONS =
            new HashMap<>();

    static {
        /*
         * widthRadius
         * heightRadius
         * depthRadius
         * centerYOffset
         *
         * These are render-space values for the barrier,
         * not Minecraft collision-box dimensions.
         */

        register(
                EntityType.ZOMBIE,
                0.85F,
                1.20F,
                0.85F,
                0.45F
        );

        register(
                EntityType.SKELETON,
                0.75F,
                1.20F,
                0.75F,
                0.45F
        );

        register(
                EntityType.CREEPER,
                0.65F,
                1.10F,
                0.65F,
                0.75F
        );

        register(
                EntityType.SPIDER,
                1.35F,
                0.70F,
                1.35F,
                1.15F
        );

        register(
                EntityType.ENDERMAN,
                0.85F,
                1.75F,
                0.85F,
                0.25F
        );

        register(
                EntityType.WARDEN,
                1.45F,
                1.75F,
                1.45F,
                0.05F
        );
    }


    private SpikedBarrierDimensions() {
    }


    private static void register(
            EntityType<?> type,
            float radiusX,
            float radiusY,
            float radiusZ,
            float centerYOffset
    ) {
        DIMENSIONS.put(
                type,
                new Dimensions(
                        radiusX,
                        radiusY,
                        radiusZ,
                        centerYOffset
                )
        );
    }


    public static Dimensions get(
            Mob mob
    ) {
        if (mob == null) {
            return null;
        }

        return DIMENSIONS.get(
                mob.getType()
        );
    }


    public record Dimensions(
            float radiusX,
            float radiusY,
            float radiusZ,
            float centerYOffset
    ) {
    }
}