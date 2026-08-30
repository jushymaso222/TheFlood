package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public final class MilestoneDefinition {

    private final ResourceLocation id;

    private final Component title;
    private final Component description;

    private final int progressionValue;
    private final double floodXpReward;

    private final Predicate<ServerPlayer> condition;


    public MilestoneDefinition(
            ResourceLocation id,
            Component title,
            Component description,
            int progressionValue,
            double floodXpReward,
            Predicate<ServerPlayer> condition
    ) {
        this.id =
                id;

        this.title =
                title;

        this.description =
                description;

        this.progressionValue =
                Math.max(
                        0,
                        Math.min(
                                100,
                                progressionValue
                        )
                );

        this.floodXpReward =
                Math.max(
                        0.0D,
                        floodXpReward
                );

        this.condition =
                condition;
    }


    public ResourceLocation id() {
        return id;
    }

    public Component title() {
        return title;
    }

    public Component description() {
        return description;
    }

    public int progressionValue() {
        return progressionValue;
    }

    public double floodXpReward() {
        return floodXpReward;
    }

    public boolean matches(
            ServerPlayer player
    ) {
        return condition != null
                && condition.test(
                        player
                );
    }
}