package com.jushymaso222.theflood;

import com.jushymaso222.theflood.config.TheFloodClientConfig;
import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.config.TheFloodConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import com.jushymaso222.theflood.compat.mekanism.TurretPlacementEvents;
import com.jushymaso222.theflood.elite.presentation.particle.EliteParticles;
import net.minecraftforge.common.MinecraftForge;
import com.jushymaso222.theflood.sound.FloodSounds;
import com.jushymaso222.theflood.progression.milestone.MilestoneRegistry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.jushymaso222.theflood.elite.drops.boon.BoonItems;
import com.jushymaso222.theflood.elite.drops.boon.effect.BoonEffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

@Mod(TheFlood.MOD_ID)
public class TheFlood {

    public static final String MOD_ID = "theflood";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheFlood() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.SERVER,
                TheFloodConfig.SERVER_CONFIG,
                "theflood-server.toml"
        );

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                TheFloodClientConfig.CLIENT_CONFIG,
                "theflood-client.toml"
        );

        IEventBus modEventBus =
                FMLJavaModLoadingContext.get()
                        .getModEventBus();

        EliteParticles.PARTICLES.register(
                modEventBus
        );

        FloodSounds.SOUNDS.register(
                modEventBus
        );

        BoonItems.register(
                modEventBus
        );

        BoonEffects.register(
                modEventBus
        );

        MilestoneRegistry.bootstrap();

        MinecraftForge.EVENT_BUS.register(
                new TurretPlacementEvents()
        );

        FloodNetwork.register();

        LOGGER.info("The Flood has loaded!");
    }
}
