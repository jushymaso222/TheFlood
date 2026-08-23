package com.jushymaso222.theflood.elite.drops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public final class EliteDropTableLoader
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON =
            new GsonBuilder()
                    .create();

    private static EliteDropTable table;


    public EliteDropTableLoader() {
        /*
         * This directory is relative to:
         *
         * data/<namespace>/
         *
         * So:
         *
         * data/theflood/elite/drops/drop_table.json
         *
         * becomes:
         *
         * theflood:elite/drops/drop_table
         */
        super(
                GSON,
                "elite/drops"
        );
    }


    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        ResourceLocation tableId =
                new ResourceLocation(
                        TheFlood.MOD_ID,
                        "drop_table"
                );

        JsonElement json =
                objects.get(
                        tableId
                );

        if (json == null) {
            TheFlood.LOGGER.error(
                    "Elite drop table was not found."
            );

            table = null;

            return;
        }

        try {
            table =
                    GSON.fromJson(
                            json,
                            EliteDropTable.class
                    );

            TheFlood.LOGGER.info(
                    "Loaded Elite drop table."
            );
        }
        catch (Exception exception) {
            TheFlood.LOGGER.error(
                    "Failed to parse Elite drop table.",
                    exception
            );

            table = null;
        }
    }


    public static EliteDropTable get() {
        return table;
    }
}