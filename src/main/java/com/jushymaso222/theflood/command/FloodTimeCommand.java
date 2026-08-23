package com.jushymaso222.theflood.command;

import com.jushymaso222.theflood.event.TimeEvents;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class FloodTimeCommand {

    private FloodTimeCommand() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("time")
                        .requires(
                                source ->
                                        source.hasPermission(2)
                        )
                        .then(
                                Commands.literal("set")

                                        .then(
                                                Commands.literal("day")
                                                        .executes(
                                                                context ->
                                                                        setTime(
                                                                                context.getSource(),
                                                                                1000L
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("noon")
                                                        .executes(
                                                                context ->
                                                                        setTime(
                                                                                context.getSource(),
                                                                                6000L
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("night")
                                                        .executes(
                                                                context ->
                                                                        setTime(
                                                                                context.getSource(),
                                                                                13000L
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.literal("midnight")
                                                        .executes(
                                                                context ->
                                                                        setTime(
                                                                                context.getSource(),
                                                                                18000L
                                                                        )
                                                        )
                                        )

                                        .then(
                                                Commands.argument(
                                                        "time",
                                                        IntegerArgumentType.integer(
                                                                0
                                                        )
                                                )
                                                        .executes(
                                                                context ->
                                                                        setTime(
                                                                                context.getSource(),
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "time"
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int setTime(
            CommandSourceStack source,
            long requestedTime
    ) {
        ServerLevel level =
                source.getLevel();

        long resolvedTime =
                TimeEvents.resolveTimeSet(
                        requestedTime
                );

        TimeEvents.setCustomWorldTime(
                resolvedTime
        );

        level.setDayTime(
                resolvedTime
        );

        source.sendSuccess(
                () ->
                        Component.literal(
                                "Set time to "
                                        + requestedTime
                        ),
                true
        );

        return (int) (
                resolvedTime
                        % 24000L
        );
    }
}