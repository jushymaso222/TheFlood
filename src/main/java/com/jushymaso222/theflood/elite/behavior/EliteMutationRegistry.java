package com.jushymaso222.theflood.elite.behavior;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.jushymaso222.theflood.elite.behavior.mutations.BulwarkMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.CommanderMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.FrenziedMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.InfestedMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.PursuerMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.ShiftingMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.SpikedMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.UndyingMutation;
import com.jushymaso222.theflood.elite.behavior.mutations.MimicMutation;

import net.minecraft.world.entity.Mob;

public final class EliteMutationRegistry {

    private static final Random RANDOM =
            new Random();

    private static final Map<String, EliteMutation> MUTATIONS =
            new LinkedHashMap<>();

    public static List<EliteMutation> all() {
        return List.copyOf(
                MUTATIONS.values()
        );
        }

    static {
        register(new BulwarkMutation());
        register(new PursuerMutation());
        register(new CommanderMutation());
        register(new FrenziedMutation());
        register(new SpikedMutation());
        register(new UndyingMutation());
        register(new InfestedMutation());
        register(new ShiftingMutation());
        register(new MimicMutation());
    }

    private EliteMutationRegistry() {
    }

    public static List<EliteMutation> validFor(
                Mob mob
        ) {
        return all()
                .stream()
                .filter(
                        mutation ->
                                mutation != null
                                && mutation.canApplyTo(
                                        mob
                                )
                )
                .toList();
        }

        public static EliteMutation randomFor(
                Mob mob
        ) {
        List<EliteMutation> valid =
                validFor(
                        mob
                );

        if (valid.isEmpty()) {
                return null;
        }

        return valid.get(
                mob.getRandom()
                        .nextInt(
                                valid.size()
                        )
        );
        }

    private static void register(
            EliteMutation mutation
    ) {
        MUTATIONS.put(
                mutation.id(),
                mutation
        );
    }

    public static EliteMutation get(
            String id
    ) {
        if (id == null) {
            return null;
        }

        return MUTATIONS.get(
                id.toLowerCase()
        );
    }

    public static EliteMutation random() {
        if (MUTATIONS.isEmpty()) {
            return null;
        }

        List<EliteMutation> available =
                new ArrayList<>(
                        MUTATIONS.values()
                );

        return available.get(
                RANDOM.nextInt(
                        available.size()
                )
        );
    }

    public static boolean exists(
            String id
    ) {
        return get(id) != null;
    }

    public static List<EliteMutation> getAll() {
        return List.copyOf(
                MUTATIONS.values()
        );
    }
}