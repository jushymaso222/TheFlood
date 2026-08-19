package com.jushymaso222.theflood.elite;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;

public final class EliteData {

    private static final String ELITE_KEY =
            "theflood_elite";

    private static final String MUTATION_KEY =
            "theflood_elite_mutation";

    private static final String ATTRIBUTES_KEY =
        "theflood_elite_attributes";

    private static final String SOURCE_HEAT_KEY =
        "theflood_elite_source_heat";

public static int getSourceHeat(
        Mob mob
) {
    return mob.getPersistentData()
            .getInt(
                    SOURCE_HEAT_KEY
            );
}

public static void setSourceHeat(
        Mob mob,
        int heat
) {
    mob.getPersistentData()
            .putInt(
                    SOURCE_HEAT_KEY,
                    heat
            );
}

    public static List<String> getAttributes(
            Mob mob
    ) {
        List<String> attributes =
                new ArrayList<>();

        ListTag tagList =
                mob.getPersistentData()
                        .getList(
                                ATTRIBUTES_KEY,
                                Tag.TAG_STRING
                        );

        for (
                int i = 0;
                i < tagList.size();
                i++
        ) {
            attributes.add(
                    tagList.getString(i)
            );
        }

        return attributes;
    }

    private EliteData() {
    }

    public static boolean isElite(
            Mob mob
    ) {
        return mob.getPersistentData()
                .getBoolean(
                        ELITE_KEY
                );
    }

    public static void setElite(
            Mob mob,
            boolean elite
    ) {
        mob.getPersistentData()
                .putBoolean(
                        ELITE_KEY,
                        elite
                );
    }

    public static void setAttributes(
            Mob mob,
            List<String> attributes
    ) {
        ListTag tagList =
                new ListTag();

        for (
                String attribute :
                attributes
        ) {
            tagList.add(
                    StringTag.valueOf(
                            attribute
                    )
            );
        }

        mob.getPersistentData()
                .put(
                        ATTRIBUTES_KEY,
                        tagList
                );
    }

    public static String getMutation(
                Mob mob
        ) {
        return mob.getPersistentData()
                .getString(
                        MUTATION_KEY
                );
        }

        public static void setMutation(
                Mob mob,
                String mutation
        ) {
        mob.getPersistentData()
                .putString(
                        MUTATION_KEY,
                        mutation
                );
        }
}