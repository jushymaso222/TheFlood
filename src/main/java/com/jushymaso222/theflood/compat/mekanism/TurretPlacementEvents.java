package com.jushymaso222.theflood.compat.mekanism;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class TurretPlacementEvents {

    public TurretPlacementEvents() {
        // System.out.println(
        //         "[The Flood Turret Debug] TurretPlacementEvents instance created"
        // );
    }

    @SubscribeEvent
    public void onBlockPlaced(
            BlockEvent.EntityPlaceEvent event
        ) {
        //         if (
        //         event.getEntity()
        //         instanceof ServerPlayer player
        // ) {
        //         // player.sendSystemMessage(
        //         //         net.minecraft.network.chat.Component.literal(
        //         //                 "[The Flood Turret Debug] EntityPlaceEvent fired"
        //         //                 + " pos=" + event.getPos()
        //         //                 + " entity="
        //         //                 + (
        //         //                         event.getEntity() == null
        //         //                                 ? "null"
        //         //                                 : event.getEntity()
        //         //                                         .getClass()
        //         //                                         .getName()
        //         //                 )
        //         //                 + " block="
        //         //                 + ForgeRegistries.BLOCKS.getKey(
        //         //                         event.getPlacedBlock()
        //         //                                 .getBlock()
        //         //                 )
        //         //         )
        //         // );
        // }

        // System.out.println(
        //         "[The Flood Turret Debug] EntityPlaceEvent fired"
        //                 + " pos=" + event.getPos()
        //                 + " entity="
        //                 + (
        //                         event.getEntity() == null
        //                                 ? "null"
        //                                 : event.getEntity()
        //                                         .getClass()
        //                                         .getName()
        //                 )
        //                 + " block="
        //                 + ForgeRegistries.BLOCKS.getKey(
        //                         event.getPlacedBlock()
        //                                 .getBlock()
        //                 )
        // );

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ResourceLocation blockId =
                ForgeRegistries.BLOCKS.getKey(
                        event.getPlacedBlock()
                                .getBlock()
                );

        if (blockId == null) {
            return;
        }

        if (!"mekanism_turrets".equals(
                blockId.getNamespace()
        )) {
            return;
        }

        TurretOwnershipManager.register(
                level,
                event.getPos(),
                player
        );

        // player.sendSystemMessage(
        //         net.minecraft.network.chat.Component.literal(
        //                 "[The Flood Turret Debug] REGISTERED "
        //                         + blockId
        //                         + " at "
        //                         + event.getPos()
        //                         + " owner="
        //                         + player.getGameProfile().getName()
        //         )
        // );

        // System.out.println(
        //         "[The Flood Turret Debug] REGISTERED "
        //                 + blockId
        //                 + " owner="
        //                 + player.getUUID()
        // );
    }

    @SubscribeEvent
    public void onBlockBroken(
            BlockEvent.BreakEvent event
    ) {
        // System.out.println(
        //         "[The Flood Turret Debug] BreakEvent fired at "
        //                 + event.getPos()
        // );

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        TurretOwnershipManager.remove(
                level,
                event.getPos()
        );
    }
}