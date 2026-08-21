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

import java.util.List;

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