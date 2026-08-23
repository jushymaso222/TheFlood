package com.jushymaso222.theflood.elite.drops.boon.effect;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.world.effect.MobEffect;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BoonEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(
                    ForgeRegistries.MOB_EFFECTS,
                    TheFlood.MOD_ID
            );


    public static final RegistryObject<MobEffect> BOON_HOARDERS =
            EFFECTS.register(
                    "boon_hoarders",
                    () ->
                            new BoonStatusEffect(
                                    0xE6C94A
                            )
            );


    public static final RegistryObject<MobEffect> BOON_ATTACK =
            EFFECTS.register(
                    "boon_attack",
                    () ->
                            new BoonStatusEffect(
                                    0xD94A4A
                            )
            );


    public static final RegistryObject<MobEffect> BOON_DEFENSE =
            EFFECTS.register(
                    "boon_defense",
                    () ->
                            new BoonStatusEffect(
                                    0x4A7ED9
                            )
            );


    public static final RegistryObject<MobEffect> BOON_TRANQUILITY =
            EFFECTS.register(
                    "boon_tranquility",
                    () ->
                            new BoonStatusEffect(
                                    0x55D98B
                            )
            );


    private BoonEffects() {
    }


    public static void register(
            IEventBus eventBus
    ) {
        EFFECTS.register(
                eventBus
        );
    }
}