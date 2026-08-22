package com.jushymaso222.theflood.elite.presentation.particle;

import com.jushymaso222.theflood.TheFlood;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EliteParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(
                    ForgeRegistries.PARTICLE_TYPES,
                    TheFlood.MOD_ID
            );


    public static final RegistryObject<
            ParticleType<EliteEnergyParticleOptions>
            > ELITE_ENERGY =
            PARTICLES.register(
                    "elite_energy",
                    () ->
                            new ParticleType<EliteEnergyParticleOptions>(
                                    false,
                                    EliteEnergyParticleOptions.DESERIALIZER
                            ) {
                                @Override
                                public Codec<EliteEnergyParticleOptions> codec() {
                                    return EliteEnergyParticleOptions.CODEC;
                                }
                            }
            );


    private EliteParticles() {
    }
}