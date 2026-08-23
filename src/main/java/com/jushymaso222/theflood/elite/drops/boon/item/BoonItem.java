package com.jushymaso222.theflood.elite.drops.boon.item;

import com.jushymaso222.theflood.elite.drops.boon.BoonType;

import com.jushymaso222.theflood.elite.drops.boon.BoonManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item;

public final class BoonItem
        extends Item {

    private final BoonType boonType;


    public BoonItem(
            BoonType boonType,
            Properties properties
    ) {
        super(
                properties
        );

        this.boonType =
                boonType;
    }


    public BoonType getBoonType() {
        return boonType;
    }

    @Override
    public boolean isFoil(
            ItemStack stack
    ) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(
                        hand
                );

        /*
        * Actual activation only happens server-side.
        */
        if (
                level.isClientSide
                || !(player instanceof ServerPlayer serverPlayer)
        ) {
            return InteractionResultHolder.pass(
                    stack
            );
        }

        boolean activated =
                BoonManager.tryActivate(
                        serverPlayer,
                        boonType
                );

        if (!activated) {
            serverPlayer.displayClientMessage(
                    Component.literal(
                            "You already have an active Boon."
                    ),
                    true
            );

            /*
            * Failed activation does NOT consume it.
            */
            return InteractionResultHolder.fail(
                    stack
            );
        }

        stack.shrink(
                1
        );

        serverPlayer.displayClientMessage(
                Component.literal(
                        boonType.displayName()
                                + " activated!"
                ),
                true
        );

        return InteractionResultHolder.consume(
                stack
        );
    }
}