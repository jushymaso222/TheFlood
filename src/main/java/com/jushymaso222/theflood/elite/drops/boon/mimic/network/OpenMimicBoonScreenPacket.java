package com.jushymaso222.theflood.elite.drops.boon.mimic.network;

import com.jushymaso222.theflood.elite.drops.boon.mimic.client.MimicBoonScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class OpenMimicBoonScreenPacket {

    public OpenMimicBoonScreenPacket() {
    }


    public static void encode(
            OpenMimicBoonScreenPacket packet,
            FriendlyByteBuf buffer
    ) {
        /*
         * No data needed.
         */
    }


    public static OpenMimicBoonScreenPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new OpenMimicBoonScreenPacket();
    }


    public static void handle(
            OpenMimicBoonScreenPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {
                    Minecraft minecraft =
                            Minecraft.getInstance();

                    minecraft.setScreen(
                            new MimicBoonScreen()
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}