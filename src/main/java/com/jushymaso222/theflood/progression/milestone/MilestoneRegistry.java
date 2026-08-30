package com.jushymaso222.theflood.progression.milestone;

import com.jushymaso222.theflood.TheFlood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;

import com.jushymaso222.theflood.progression.milestone.compat.BotaniaMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.DraconicEvolutionMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.MekanismMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.PneumaticCraftMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.ProjectEMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.TaCZMilestones;
import com.jushymaso222.theflood.progression.milestone.compat.TwilightForestMilestones;

import static com.jushymaso222.theflood.progression.milestone.MilestoneConditions.*;

public final class MilestoneRegistry {

    private static final List<MilestoneDefinition>
            MILESTONES =
            new ArrayList<>();

    private MilestoneRegistry() {
    }

    /*
     * ============================================
     * BOOTSTRAP
     * ============================================
     */

    public static void bootstrap() {

        if (!MILESTONES.isEmpty()) {
            return;
        }

        /*
         * Vanilla milestones live directly here.
         *
         * Built-in mod compatibility will be
         * registered below these once the compat
         * classes are created.
         */

        registerVanillaMilestones();


        /*
         * ============================================
         * BUILT-IN MOD COMPATIBILITY
         * ============================================
        */

         MekanismMilestones.register();
         BotaniaMilestones.register();
         DraconicEvolutionMilestones.register();
         PneumaticCraftMilestones.register();
         ProjectEMilestones.register();
         TwilightForestMilestones.register();
         TaCZMilestones.register();

    }


    /*
     * ============================================
     * VANILLA MILESTONES
     * ============================================
     */

    private static void registerVanillaMilestones() {

        /*
        * ============================================
        * EARLY VANILLA
        * ============================================
        */

        registerVanilla(
                "iron_age",
                "Iron Age",
                "You've begun preparing for the fight ahead.",
                10,
                0.02D,
                any(
                        hasItem(
                                Items.IRON_INGOT
                        ),
                        hasAdvancement(
                                "minecraft:story/smelt_iron"
                        )
                )
        );

        registerVanilla(
                "diamond_age",
                "Diamond Age",
                "You've discovered something much stronger.",
                20,
                0.03D,
                any(
                        hasItem(
                                Items.DIAMOND
                        ),
                        hasAdvancement(
                                "minecraft:story/mine_diamond"
                        )
                )
        );


        /*
        * ============================================
        * ESTABLISHED VANILLA
        * ============================================
        */

        registerVanilla(
                "nether",
                "Into the Nether",
                "You've ventured beyond the Overworld.",
                30,
                0.04D,
                hasAdvancement(
                        "minecraft:story/enter_the_nether"
                )
        );

        registerVanilla(
                "enchanting",
                "Arcane Advantage",
                "You've begun enhancing your equipment.",
                35,
                0.04D,
                hasAdvancement(
                        "minecraft:story/enchant_item"
                )
        );


        /*
        * ============================================
        * VANILLA LATE GAME
        * ============================================
        */

        registerVanilla(
                "netherite",
                "Ancient Strength",
                "You've obtained the strongest material of the old world.",
                45,
                0.06D,
                any(
                        hasItem(
                                Items.NETHERITE_INGOT
                        ),
                        hasAdvancement(
                                "minecraft:nether/netherite_armor"
                        )
                )
        );

        registerVanilla(
                "the_end",
                "The End",
                "You've reached a world beyond the void.",
                50,
                0.07D,
                hasAdvancement(
                        "minecraft:story/enter_the_end"
                )
        );

        registerVanilla(
                "dragon_defeated",
                "Free the End",
                "You've defeated the Ender Dragon.",
                55,
                0.10D,
                hasAdvancement(
                        "minecraft:end/kill_dragon"
                )
        );

        registerVanilla(
                "elytra",
                "The Sky Is Yours",
                "You've gained the power of flight.",
                60,
                0.08D,
                any(
                        hasItem(
                                Items.ELYTRA
                        ),
                        hasAdvancement(
                                "minecraft:end/elytra"
                        )
                )
        );
    }


    /*
     * ============================================
     * VANILLA REGISTRATION
     * ============================================
     */

    private static void registerVanilla(
            String name,
            String title,
            String description,
            int progressionValue,
            double floodXpReward,
            Predicate<ServerPlayer> condition
    ) {
        register(
                "vanilla/" + name,
                title,
                description,
                progressionValue,
                floodXpReward,
                condition
        );
    }


    /*
     * ============================================
     * COMPAT REGISTRATION
     * ============================================
     */

    public static void registerCompat(
            String modId,
            String name,
            String title,
            String description,
            int progressionValue,
            double floodXpReward,
            Predicate<ServerPlayer> condition
    ) {
        register(
                modId + "/" + name,
                title,
                description,
                progressionValue,
                floodXpReward,
                condition
        );
    }


    /*
     * ============================================
     * GENERAL REGISTRATION
     * ============================================
     */

    public static void register(
            String name,
            String title,
            String description,
            int progressionValue,
            double floodXpReward,
            Predicate<ServerPlayer> condition
    ) {
        register(
                new MilestoneDefinition(
                        new ResourceLocation(
                                TheFlood.MOD_ID,
                                name
                        ),
                        Component.literal(
                                title
                        ),
                        Component.literal(
                                description
                        ),
                        progressionValue,
                        floodXpReward,
                        condition
                )
        );
    }

    public static void register(
            MilestoneDefinition milestone
    ) {
        if (milestone == null) {
            return;
        }

        MILESTONES.add(
                milestone
        );
    }


    /*
     * ============================================
     * ACCESS
     * ============================================
     */

    public static List<MilestoneDefinition> all() {
        return Collections.unmodifiableList(
                MILESTONES
        );
    }
}