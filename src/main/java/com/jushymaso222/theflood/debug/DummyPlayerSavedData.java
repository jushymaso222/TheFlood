package com.jushymaso222.theflood.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.Registries;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DummyPlayerSavedData extends SavedData {

    private static final String DATA_NAME =
            "theflood_dummy_players";

    public record DummyData(
            String name,
            ResourceKey<Level> dimension,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            int soloHeat
    ) {
    }

    private final Map<String, DummyData> dummies =
            new LinkedHashMap<>();

    public static DummyPlayerSavedData get(
            MinecraftServer server
    ) {
        ServerLevel overworld =
                server.overworld();

        return overworld.getDataStorage()
                .computeIfAbsent(
                        DummyPlayerSavedData::load,
                        DummyPlayerSavedData::new,
                        DATA_NAME
                );
    }

    public Collection<DummyData> getDummies() {
        return dummies.values();
    }

    public void put(
            DummyData data
    ) {
        dummies.put(
                data.name()
                        .trim()
                        .toLowerCase(),
                data
        );

        setDirty();
    }

    public void remove(
            String name
    ) {
        dummies.remove(
                name.trim()
                        .toLowerCase()
        );

        setDirty();
    }

    public void clear() {
        dummies.clear();
        setDirty();
    }

    @Override
    public CompoundTag save(
            CompoundTag tag
    ) {
        ListTag list =
                new ListTag();

        for (DummyData data :
                dummies.values()) {

            CompoundTag dummyTag =
                    new CompoundTag();

            dummyTag.putString(
                    "Name",
                    data.name()
            );

            dummyTag.putString(
                    "Dimension",
                    data.dimension()
                            .location()
                            .toString()
            );

            dummyTag.putDouble(
                    "X",
                    data.x()
            );

            dummyTag.putDouble(
                    "Y",
                    data.y()
            );

            dummyTag.putDouble(
                    "Z",
                    data.z()
            );

            dummyTag.putFloat(
                    "Yaw",
                    data.yaw()
            );

            dummyTag.putFloat(
                    "Pitch",
                    data.pitch()
            );

            dummyTag.putInt(
                    "SoloHeat",
                    data.soloHeat()
            );

            list.add(
                    dummyTag
            );
        }

        tag.put(
                "Dummies",
                list
        );

        return tag;
    }

    private static DummyPlayerSavedData load(
            CompoundTag tag
    ) {
        DummyPlayerSavedData data =
                new DummyPlayerSavedData();

        ListTag list =
                tag.getList(
                        "Dummies",
                        Tag.TAG_COMPOUND
                );

        for (int i = 0; i < list.size(); i++) {

            CompoundTag dummyTag =
                    list.getCompound(i);

            String name =
                    dummyTag.getString(
                            "Name"
                    );

            ResourceLocation dimensionId =
                    new ResourceLocation(
                            dummyTag.getString(
                                    "Dimension"
                            )
                    );

            ResourceKey<Level> dimension =
                ResourceKey.create(
                        Registries.DIMENSION,
                        dimensionId
                );

            DummyData dummy =
                    new DummyData(
                            name,
                            dimension,
                            dummyTag.getDouble("X"),
                            dummyTag.getDouble("Y"),
                            dummyTag.getDouble("Z"),
                            dummyTag.getFloat("Yaw"),
                            dummyTag.getFloat("Pitch"),
                            dummyTag.getInt("SoloHeat")
                    );

            data.dummies.put(
                    name.trim()
                            .toLowerCase(),
                    dummy
            );
        }

        return data;
    }
}