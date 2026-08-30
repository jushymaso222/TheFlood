package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class MilestoneData {

    private static final String ROOT_KEY =
            "theflood_milestones";

    /*
     * Ordered list of completed milestone IDs.
     *
     * Oldest completion is first.
     * Newest completion is last.
     */
    private static final String HISTORY_KEY =
            "completion_history";


    private MilestoneData() {
    }


    private static CompoundTag getData(
            ServerPlayer player
    ) {
        CompoundTag persistent =
                player.getPersistentData();

        if (
                !persistent.contains(
                        ROOT_KEY
                )
        ) {
            persistent.put(
                    ROOT_KEY,
                    new CompoundTag()
            );
        }

        return persistent.getCompound(
                ROOT_KEY
        );
    }


    /*
     * =================================================
     * COMPLETION STATE
     * =================================================
     */

    public static boolean hasAchieved(
            ServerPlayer player,
            ResourceLocation id
    ) {
        return getData(
                player
        ).getBoolean(
                id.toString()
        );
    }


    /*
     * =================================================
     * MARK COMPLETED
     * =================================================
     */

    public static void markAchieved(
            ServerPlayer player,
            ResourceLocation id
    ) {
        CompoundTag data =
                getData(
                        player
                );


        /*
         * Safety against duplicate history entries.
         *
         * Normally MilestoneManager already prevents
         * markAchieved() from being called twice, but
         * keeping this guarded here makes the data
         * layer safe on its own.
         */
        if (
                data.getBoolean(
                        id.toString()
                )
        ) {
            return;
        }


        /*
         * Existing completion storage.
         *
         * Keeping this exactly as before means old
         * worlds remain compatible.
         */
        data.putBoolean(
                id.toString(),
                true
        );


        /*
         * Add the milestone to persistent completion
         * history.
         */
        ListTag history =
                data.getList(
                        HISTORY_KEY,
                        Tag.TAG_STRING
                );

        history.add(
                StringTag.valueOf(
                        id.toString()
                )
        );

        data.put(
                HISTORY_KEY,
                history
        );
    }


    /*
     * =================================================
     * COMPLETION HISTORY
     * =================================================
     */

    public static List<ResourceLocation> getCompletionHistory(
            ServerPlayer player
    ) {
        CompoundTag data =
                getData(
                        player
                );

        ListTag history =
                data.getList(
                        HISTORY_KEY,
                        Tag.TAG_STRING
                );

        List<ResourceLocation> result =
                new ArrayList<>();


        for (
                int i = 0;
                i < history.size();
                i++
        ) {
            String value =
                    history.getString(
                            i
                    );

            ResourceLocation id =
                    ResourceLocation.tryParse(
                            value
                    );

            if (id != null) {
                result.add(
                        id
                );
            }
        }


        return List.copyOf(
                result
        );
    }


    /*
     * Returns newest completions first.
     *
     * This is what the small Tab milestone panel
     * will eventually use.
     */
    public static List<ResourceLocation> getRecentCompletions(
            ServerPlayer player,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }


        List<ResourceLocation> history =
                getCompletionHistory(
                        player
                );

        List<ResourceLocation> recent =
                new ArrayList<>();


        for (
                int i = history.size() - 1;
                i >= 0
                        && recent.size() < limit;
                i--
        ) {
            recent.add(
                    history.get(
                            i
                    )
            );
        }


        return List.copyOf(
                recent
        );
    }


    /*
     * =================================================
     * PLAYER COPY
     * =================================================
     */

    public static void copy(
            ServerPlayer oldPlayer,
            ServerPlayer newPlayer
    ) {
        CompoundTag oldPersistent =
                oldPlayer.getPersistentData();

        if (
                !oldPersistent.contains(
                        ROOT_KEY
                )
        ) {
            return;
        }


        CompoundTag oldMilestones =
                oldPersistent.getCompound(
                        ROOT_KEY
                );


        newPlayer.getPersistentData()
                .put(
                        ROOT_KEY,
                        oldMilestones.copy()
                );
    }
}