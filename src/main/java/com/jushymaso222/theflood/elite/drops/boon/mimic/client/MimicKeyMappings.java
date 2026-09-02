package com.jushymaso222.theflood.elite.drops.boon.mimic.client;

import com.jushymaso222.theflood.TheFlood;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;


public final class MimicKeyMappings {

    public static final String CATEGORY =
            "key.categories.theflood";


    public static final KeyMapping EFFECT_PRIMARY =
            new KeyMapping(
                    "key.theflood.effect_primary",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    CATEGORY
            );


    public static final KeyMapping EFFECT_SECONDARY =
            new KeyMapping(
                    "key.theflood.effect_secondary",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    CATEGORY
            );


    private MimicKeyMappings() {
    }


    @Mod.EventBusSubscriber(
            modid = TheFlood.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static final class Registration {

        private Registration() {
        }


        @SubscribeEvent
        public static void registerKeyMappings(
                RegisterKeyMappingsEvent event
        ) {
            event.register(
                    EFFECT_PRIMARY
            );

            event.register(
                    EFFECT_SECONDARY
            );
        }
    }
}