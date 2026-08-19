package com.jushymaso222.theflood.guide.network;

import com.jushymaso222.theflood.guide.client.ClientServerSettingsData;
import com.jushymaso222.theflood.guide.ServerSettingEntry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncServerSettingsPacket {

    private final List<ServerSettingEntry> settings;

    public SyncServerSettingsPacket(
            List<ServerSettingEntry> settings
    ) {
        this.settings =
                settings;
    }

    public static void encode(
            SyncServerSettingsPacket message,
            FriendlyByteBuf buffer
    ) {

        buffer.writeVarInt(
                message.settings.size()
        );

        for (
                ServerSettingEntry entry :
                message.settings
        ) {

            buffer.writeUtf(
                    entry.section()
            );

            buffer.writeUtf(
                    entry.group()
            );

            buffer.writeUtf(
                    entry.name()
            );

            buffer.writeUtf(
                    entry.value()
            );

            buffer.writeUtf(
                    entry.defaultValue()
            );

            buffer.writeUtf(
                    entry.description()
            );
        }
    }

    public static SyncServerSettingsPacket decode(
            FriendlyByteBuf buffer
    ) {

        int count =
                buffer.readVarInt();

        List<ServerSettingEntry> settings =
                new ArrayList<>();

        for (
                int i = 0;
                i < count;
                i++
        ) {

            settings.add(
                    new ServerSettingEntry(
                            buffer.readUtf(),
                            buffer.readUtf(),
                            buffer.readUtf(),
                            buffer.readUtf(),
                            buffer.readUtf(),
                            buffer.readUtf()
                    )
            );
        }

        return new SyncServerSettingsPacket(
                settings
        );
    }

    public static void handle(
            SyncServerSettingsPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {

        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() ->
                ClientServerSettingsData.setSettings(
                        message.settings
                )
        );

        context.setPacketHandled(
                true
        );
    }
}