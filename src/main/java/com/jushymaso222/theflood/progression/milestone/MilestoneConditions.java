package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

import java.util.function.Predicate;

public final class MilestoneConditions {

    private MilestoneConditions() {
    }

    public static Predicate<ServerPlayer> hasItem(
            Item item
    ) {
        return player -> {
            for (
                    ItemStack stack :
                    player.getInventory().items
            ) {
                if (stack.is(item)) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<ServerPlayer> modLoaded(
            String modId
    ) {
        return player ->
                ModList.get()
                        .isLoaded(
                                modId
                        );
    }

    public static Predicate<ServerPlayer> hasItemId(
            String itemId
    ) {
        ResourceLocation id =
                ResourceLocation.tryParse(
                        itemId
                );

        if (id == null) {
            return player -> false;
        }

        return player -> {
            Item item =
                    BuiltInRegistries.ITEM.get(
                            id
                    );

            /*
            * An unknown ID resolves to AIR.
            * Don't accidentally treat that as
            * a legitimate milestone item.
            */
            if (item == Items.AIR) {
                return false;
            }

            for (
                    ItemStack stack :
                    player.getInventory().items
            ) {
                if (stack.is(item)) {
                    return true;
                }
            }

            /*
            * Also check armor.
            */
            for (
                    ItemStack stack :
                    player.getInventory().armor
            ) {
                if (stack.is(item)) {
                    return true;
                }
            }

            /*
            * And offhand.
            */
            for (
                    ItemStack stack :
                    player.getInventory().offhand
            ) {
                if (stack.is(item)) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<ServerPlayer> hasAnyItemId(
            String... itemIds
    ) {
        List<Predicate<ServerPlayer>> conditions =
                new ArrayList<>();

        for (
                String itemId :
                itemIds
        ) {
            conditions.add(
                    hasItemId(
                            itemId
                    )
            );
        }

        return any(
                conditions.toArray(
                        new Predicate[0]
                )
        );
    }

    public static Predicate<ServerPlayer> hasAllItemIds(
            String... itemIds
    ) {
        List<Predicate<ServerPlayer>> conditions =
                new ArrayList<>();

        for (
                String itemId :
                itemIds
        ) {
            conditions.add(
                    hasItemId(
                            itemId
                    )
            );
        }

        return all(
                conditions.toArray(
                        new Predicate[0]
                )
        );
    }

    public static Predicate<ServerPlayer> hasFullArmorSetIds(
            String helmetId,
            String chestplateId,
            String leggingsId,
            String bootsId
    ) {
        Predicate<ServerPlayer> helmet =
                hasItemId(
                        helmetId
                );

        Predicate<ServerPlayer> chestplate =
                hasItemId(
                        chestplateId
                );

        Predicate<ServerPlayer> leggings =
                hasItemId(
                        leggingsId
                );

        Predicate<ServerPlayer> boots =
                hasItemId(
                        bootsId
                );

        return player ->
                helmet.test(player)
                && chestplate.test(player)
                && leggings.test(player)
                && boots.test(player);
    }

    public static Predicate<ServerPlayer> hasEquipped(
            Item item
    ) {
        return player -> {
            for (
                    ItemStack stack :
                    player.getArmorSlots()
            ) {
                if (stack.is(item)) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<ServerPlayer> hasAdvancement(
            String advancementId
    ) {
        return player -> {
            ResourceLocation id;

            try {
                id =
                        new ResourceLocation(
                                advancementId
                        );
            }
            catch (Exception exception) {
                return false;
            }

            var advancement =
                    player.server
                            .getAdvancements()
                            .getAdvancement(
                                    id
                            );

            if (advancement == null) {
                return false;
            }

            return player.getAdvancements()
                    .getOrStartProgress(
                            advancement
                    )
                    .isDone();
        };
    }

    public static Predicate<ServerPlayer> any(
            Predicate<ServerPlayer>... conditions
    ) {
        return player -> {
            for (
                    Predicate<ServerPlayer> condition :
                    conditions
            ) {
                if (
                        condition != null
                        && condition.test(player)
                ) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<ServerPlayer> all(
            Predicate<ServerPlayer>... conditions
    ) {
        return player -> {
            for (
                    Predicate<ServerPlayer> condition :
                    conditions
            ) {
                if (
                        condition == null
                        || !condition.test(player)
                ) {
                    return false;
                }
            }

            return true;
        };
    }
}