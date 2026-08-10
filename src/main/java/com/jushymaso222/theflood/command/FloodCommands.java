package com.jushymaso222.theflood.command;

import com.jushymaso222.theflood.progression.PlayerFloodData;
import com.jushymaso222.theflood.progression.HeatManager;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.jushymaso222.theflood.TheFlood;

import com.jushymaso222.theflood.debug.DummyPlayerManager;
import net.minecraftforge.common.util.FakePlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FloodCommands {

    private FloodCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {
        event.getDispatcher().register(
                Commands.literal("flood")

                        .then(
                                Commands.literal("dummy")
                                        .requires(source -> source.hasPermission(2))

                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument(
                                                                        "name",
                                                                        StringArgumentType.word()
                                                                )
                                                                .executes(context -> {
                                                                ServerPlayer player =
                                                                        context.getSource()
                                                                                .getPlayerOrException();

                                                                String name =
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "name"
                                                                        );

                                                                FakePlayer dummy =
                                                                        DummyPlayerManager.addDummy(
                                                                                player,
                                                                                name
                                                                        );

                                                                if (dummy == null) {
                                                                        player.sendSystemMessage(
                                                                                Component.literal(
                                                                                        "Dummy player "
                                                                                                + name
                                                                                                + " already exists."
                                                                                ).withStyle(
                                                                                        ChatFormatting.RED
                                                                                )
                                                                        );

                                                                        return 0;
                                                                }

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "Created dummy player "
                                                                                        + name
                                                                        ).withStyle(
                                                                                ChatFormatting.GREEN
                                                                        )
                                                                );

                                                                return 1;
                                                                })
                                                        )
                                        )

                                        .then(
                                                Commands.literal("remove")
                                                        .then(
                                                                Commands.argument(
                                                                        "name",
                                                                        StringArgumentType.word()
                                                                )
                                                                .executes(context -> {
                                                                ServerPlayer player =
                                                                        context.getSource()
                                                                                .getPlayerOrException();

                                                                String name =
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "name"
                                                                        );

                                                                boolean removed =
                                                                        DummyPlayerManager.removeDummy(
                                                                                name
                                                                        );

                                                                if (!removed) {
                                                                        player.sendSystemMessage(
                                                                                Component.literal(
                                                                                        "No dummy player named "
                                                                                                + name
                                                                                                + " exists."
                                                                                ).withStyle(
                                                                                        ChatFormatting.RED
                                                                                )
                                                                        );

                                                                        return 0;
                                                                }

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "Removed dummy player "
                                                                                        + name
                                                                        ).withStyle(
                                                                                ChatFormatting.YELLOW
                                                                        )
                                                                );

                                                                return 1;
                                                                })
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("heat")
                                        .requires(source -> source.hasPermission(2))

                                        /*
                                         * /flood heat get
                                         */
                                        .then(
                                                Commands.literal("get")
                                                        .executes(context -> {
                                                        ServerPlayer player =
                                                                context.getSource()
                                                                        .getPlayerOrException();

                                                        int soloHeat =
                                                                HeatManager.getSoloHeat(player);

                                                        int teamHeat =
                                                                HeatManager.getTeamHeat(player);

                                                        int baseHeat =
                                                                HeatManager.getBaseHeat(player);

                                                        int proximityBonus =
                                                                HeatManager.getProximityHeatBonus(player);

                                                        int effectiveHeat =
                                                                HeatManager.getEffectiveHeat(player);

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "=== The Flood Heat ==="
                                                                )
                                                        );

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "Solo Heat: "
                                                                                + soloHeat
                                                                )
                                                        );

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "Team Heat: "
                                                                                + (teamHeat > 0
                                                                                ? teamHeat
                                                                                : "N/A")
                                                                )
                                                        );

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "Base Heat: "
                                                                                + baseHeat
                                                                )
                                                        );

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "Proximity Bonus: +"
                                                                                + proximityBonus
                                                                )
                                                        );

                                                        player.sendSystemMessage(
                                                                Component.literal(
                                                                        "Effective Heat: "
                                                                                + effectiveHeat
                                                                )
                                                        );

                                                        return 1;
                                                        })
                                        )

                                        /*
                                         * /flood heat set <amount>
                                         */
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType.integer(
                                                                                    1,
                                                                                    PlayerFloodData.MAX_HEAT
                                                                                )
                                                                        )
                                                                        .executes(context -> {
                                                                            ServerPlayer player =
                                                                                    context.getSource()
                                                                                            .getPlayerOrException();

                                                                            int amount =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "amount"
                                                                                    );

                                                                            setSoloHeat(
                                                                                    player,
                                                                                    amount
                                                                            );

                                                                            player.sendSystemMessage(
                                                                                    Component.literal(
                                                                                            "Solo Heat set to "
                                                                                                    + amount
                                                                                                    + ". Effective Heat: "
                                                                                                    + HeatManager.getEffectiveHeat(player)
                                                                                    )
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )

                                        /*
                                         * /flood heat add <amount>
                                         */
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType.integer(1)
                                                                        )
                                                                        .executes(context -> {
                                                                            ServerPlayer player =
                                                                                    context.getSource()
                                                                                            .getPlayerOrException();

                                                                            int amount =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "amount"
                                                                                    );

                                                                            int newHeat =
                                                                                    HeatManager.getSoloHeat(player)
                                                                                            + amount;

                                                                            setSoloHeat(
                                                                                    player,
                                                                                    newHeat
                                                                            );

                                                                            player.sendSystemMessage(
                                                                                    Component.literal(
                                                                                            "Added "
                                                                                                    + amount
                                                                                                    + " Heat. Solo Heat: "
                                                                                                    + newHeat
                                                                                                    + " | Effective Heat: "
                                                                                                    + HeatManager.getEffectiveHeat(player)
                                                                                    )
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    

    private static void setSoloHeat(
            ServerPlayer player,
            int heat
    ) {
        PlayerFloodData.setSoloHeat(
                player,
                heat
        );

        /*
        * Immediately update ClientHeatData so HUD elements
        * reflect the new Effective Heat.
        */
        HeatManager.syncHeatToPlayer(
                player
        );
    }
}