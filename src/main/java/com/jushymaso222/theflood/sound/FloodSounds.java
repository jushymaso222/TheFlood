package com.jushymaso222.theflood.sound;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class FloodSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(
                    ForgeRegistries.SOUND_EVENTS,
                    TheFlood.MOD_ID
            );

    public static final RegistryObject<SoundEvent> COMMANDER_WHISTLE =
            SOUNDS.register(
                    "elite.commander_whistle",
                    () ->
                            SoundEvent.createVariableRangeEvent(
                                    new ResourceLocation(
                                            TheFlood.MOD_ID,
                                            "elite.commander_whistle"
                                    )
                            )
            );

    private FloodSounds() {
    }
}