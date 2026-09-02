package com.jushymaso222.theflood.elite.drops.boon;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.drops.boon.item.BoonItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BoonItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    TheFlood.MOD_ID
            );


    public static final RegistryObject<Item> BOON_HOARDERS =
            ITEMS.register(
                    "boon_hoarders",
                    () ->
                            new BoonItem(
                                    BoonType.HOARDERS,
                                    new Item.Properties()
                                            .stacksTo(
                                                    1
                                            )
                            )
            );


    public static final RegistryObject<Item> BOON_ATTACK =
            ITEMS.register(
                    "boon_attack",
                    () ->
                            new BoonItem(
                                    BoonType.ATTACK,
                                    new Item.Properties()
                                            .stacksTo(
                                                    1
                                            )
                            )
            );


    public static final RegistryObject<Item> BOON_DEFENSE =
            ITEMS.register(
                    "boon_defense",
                    () ->
                            new BoonItem(
                                    BoonType.DEFENSE,
                                    new Item.Properties()
                                            .stacksTo(
                                                    1
                                            )
                            )
            );


    public static final RegistryObject<Item> BOON_TRANQUILITY =
            ITEMS.register(
                    "boon_tranquility",
                    () ->
                            new BoonItem(
                                    BoonType.TRANQUILITY,
                                    new Item.Properties()
                                            .stacksTo(
                                                    1
                                            )
                            )
            );

    public static final RegistryObject<Item> BOON_MIMIC =
        ITEMS.register(
                "boon_mimic",
                () ->
                        new BoonItem(
                                BoonType.MIMIC,
                                new Item.Properties()
                                    .stacksTo(1)
                        )
        );


    private BoonItems() {
    }


    public static void register(
            IEventBus eventBus
    ) {
        ITEMS.register(
                eventBus
        );
    }
}