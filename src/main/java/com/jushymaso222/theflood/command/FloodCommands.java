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
import com.jushymaso222.theflood.elite.EliteStateSync;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.jushymaso222.theflood.elite.EliteManager;
import com.jushymaso222.theflood.elite.debug.EliteDebugSpec;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.jushymaso222.theflood.TheFlood;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;
import com.jushymaso222.theflood.progression.capability.CapabilityProfile;
import com.jushymaso222.theflood.progression.milestone.MilestoneManager;

import com.jushymaso222.theflood.elite.debug.EliteStatsWatch;
import com.jushymaso222.theflood.elite.debug.network.SyncMobStatsPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.jushymaso222.theflood.debug.DummyPlayerManager;
import net.minecraftforge.common.util.FakePlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;

import com.jushymaso222.theflood.team.TeamChatManager;
import com.jushymaso222.theflood.team.FloodTeam;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.team.network.TeamNetworkingPackets;

import net.minecraftforge.network.PacketDistributor;
import com.jushymaso222.theflood.progression.capability.debug.CapabilityStatsWatch;
import com.jushymaso222.theflood.progression.capability.network.SyncCapabilityStatsPacket;

import com.jushymaso222.theflood.team.TeamManager;

import java.util.UUID;

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
        LiteralArgumentBuilder<CommandSourceStack> flood
                = Commands.literal("flood");

        flood.then(
                Commands.literal("version")
                        .executes(context -> {

                            String version
                                    = net.minecraftforge.fml.ModList
                                            .get()
                                            .getModContainerById(
                                                    TheFlood.MOD_ID
                                            )
                                            .map(
                                                    container
                                                    -> container.getModInfo()
                                                            .getVersion()
                                                            .toString()
                                            )
                                            .orElse(
                                                    "Unknown"
                                            );

                            context.getSource()
                                    .sendSuccess(
                                            ()
                                            -> Component.literal(
                                                    "The Flood v"
                                                    + version
                                            ).withStyle(
                                                    ChatFormatting.DARK_RED
                                            ),
                                            false
                                    );

                            return 1;
                        })
        );

        if (!FMLEnvironment.production) {

            flood.then(
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
                                                                ServerPlayer player
                                                                        = context.getSource()
                                                                                .getPlayerOrException();

                                                                String name
                                                                        = StringArgumentType.getString(
                                                                                context,
                                                                                "name"
                                                                        );

                                                                FakePlayer dummy
                                                                        = DummyPlayerManager.addDummy(
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
                                                                ServerPlayer player
                                                                        = context.getSource()
                                                                                .getPlayerOrException();

                                                                String name
                                                                        = StringArgumentType.getString(
                                                                                context,
                                                                                "name"
                                                                        );

                                                                boolean removed
                                                                        = DummyPlayerManager.removeDummy(
                                                                                player.server,
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
                            .then(
                                    Commands.literal("clear")
                                            .executes(context -> {

                                                ServerPlayer player
                                                        = context.getSource()
                                                                .getPlayerOrException();

                                                int removed
                                                        = DummyPlayerManager.clearAll(
                                                                player.server
                                                        );

                                                player.sendSystemMessage(
                                                        Component.literal(
                                                                "Cleared "
                                                                + removed
                                                                + " dummy player"
                                                                + (removed == 1
                                                                        ? "."
                                                                        : "s.")
                                                        ).withStyle(
                                                                ChatFormatting.YELLOW
                                                        )
                                                );

                                                return removed;
                                            })
                            )
            );
        }

        flood.then(
                Commands.literal("heat")
                        .requires(source -> source.hasPermission(2))
                        /*
                                * /flood heat get
                         */
                        .then(
                                Commands.literal("get")
                                        .executes(context -> {
                                            ServerPlayer player
                                                    = context.getSource()
                                                            .getPlayerOrException();

                                            int soloHeat
                                                    = HeatManager.getSoloHeat(player);

                                            int teamHeat
                                                    = HeatManager.getTeamHeat(player);

                                            int baseHeat
                                                    = HeatManager.getBaseHeat(player);

                                            int proximityBonus
                                                    = HeatManager.getProximityHeatBonus(player);

                                            int effectiveHeat
                                                    = HeatManager.getEffectiveHeat(player);

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
                                                            ServerPlayer player
                                                                    = context.getSource()
                                                                            .getPlayerOrException();

                                                            int amount
                                                                    = IntegerArgumentType.getInteger(
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
                                                            ServerPlayer player
                                                                    = context.getSource()
                                                                            .getPlayerOrException();

                                                            int amount
                                                                    = IntegerArgumentType.getInteger(
                                                                            context,
                                                                            "amount"
                                                                    );

                                                            int newHeat
                                                                    = HeatManager.getSoloHeat(player)
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
        );

        LiteralArgumentBuilder<CommandSourceStack> team
                = Commands.literal(
                        "team"
                );

        team.then(
                Commands.literal("confirm")
                        .then(
                                Commands.argument(
                                        "teamId",
                                        StringArgumentType.word()
                                )
                                        .executes(context -> {
                                            ServerPlayer player
                                                    = context.getSource()
                                                            .getPlayerOrException();

                                            String rawTeamId
                                                    = StringArgumentType.getString(
                                                            context,
                                                            "teamId"
                                                    );

                                            UUID teamId;

                                            try {
                                                teamId
                                                        = UUID.fromString(
                                                                rawTeamId
                                                        );
                                            } catch (IllegalArgumentException exception) {
                                                player.sendSystemMessage(
                                                        Component.literal(
                                                                "Invalid team confirmation."
                                                        ).withStyle(
                                                                ChatFormatting.RED
                                                        )
                                                );

                                                return 0;
                                            }

                                            boolean joined
                                                    = TeamManager.confirmHeatInvite(
                                                            player,
                                                            teamId
                                                    );

                                            return joined
                                                    ? 1
                                                    : 0;
                                        })
                        )
        );

        team.then(
                Commands.literal("cancel")
                        .then(
                                Commands.argument(
                                        "teamId",
                                        StringArgumentType.word()
                                )
                                        .executes(context -> {
                                            ServerPlayer player
                                                    = context.getSource()
                                                            .getPlayerOrException();

                                            String rawTeamId
                                                    = StringArgumentType.getString(
                                                            context,
                                                            "teamId"
                                                    );

                                            UUID teamId;

                                            try {
                                                teamId
                                                        = UUID.fromString(
                                                                rawTeamId
                                                        );
                                            } catch (IllegalArgumentException exception) {
                                                return 0;
                                            }

                                            TeamManager.cancelHeatInvite(
                                                    player,
                                                    teamId
                                            );

                                            player.sendSystemMessage(
                                                    Component.literal(
                                                            "Team invitation cancelled."
                                                    ).withStyle(
                                                            ChatFormatting.YELLOW
                                                    )
                                            );

                                            return 1;
                                        })
                        )
        );

        team.then(
                Commands.literal("chat")
                        .executes(context -> {
                            ServerPlayer player
                                    = context.getSource()
                                            .getPlayerOrException();

                            TeamChatManager.ChatMode mode
                                    = TeamChatManager.getMode(
                                            player.getUUID()
                                    );

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Team chat mode: "
                                            + mode.name()
                                    ).withStyle(
                                            ChatFormatting.YELLOW
                                    )
                            );

                            return 1;
                        })
                        .then(
                                Commands.literal("global")
                                        .executes(context -> {
                                            ServerPlayer player
                                                    = context.getSource()
                                                            .getPlayerOrException();

                                            TeamChatManager.setMode(
                                                    player.getUUID(),
                                                    TeamChatManager.ChatMode.GLOBAL
                                            );

                                            FloodNetwork.CHANNEL.send(
                                                    PacketDistributor.PLAYER.with(
                                                            () -> player
                                                    ),
                                                    new TeamNetworkingPackets.SyncTeamChatModePacket(
                                                            false
                                                    )
                                            );

                                            player.sendSystemMessage(
                                                    Component.literal(
                                                            "Chat mode set to Global."
                                                    ).withStyle(
                                                            ChatFormatting.GREEN
                                                    )
                                            );

                                            return 1;
                                        })
                        )
                        .then(
                                Commands.literal("team")
                                        .executes(context -> {
                                            ServerPlayer player
                                                    = context.getSource()
                                                            .getPlayerOrException();

                                            FloodTeam playerTeam
                                                    = TeamManager.getTeamForPlayer(
                                                            player
                                                    );

                                            if (playerTeam == null) {
                                                player.sendSystemMessage(
                                                        Component.literal(
                                                                "You are not in a team."
                                                        ).withStyle(
                                                                ChatFormatting.RED
                                                        )
                                                );

                                                return 0;
                                            }

                                            TeamChatManager.setMode(
                                                    player.getUUID(),
                                                    TeamChatManager.ChatMode.TEAM
                                            );

                                            FloodNetwork.CHANNEL.send(
                                                    PacketDistributor.PLAYER.with(
                                                            () -> player
                                                    ),
                                                    new TeamNetworkingPackets.SyncTeamChatModePacket(
                                                            true
                                                    )
                                            );

                                            player.sendSystemMessage(
                                                    Component.literal(
                                                            "Chat mode set to Team."
                                                    ).withStyle(
                                                            ChatFormatting.AQUA
                                                    )
                                            );

                                            return 1;
                                        })
                        )
        );

        if (!FMLEnvironment.production) {
            team.then(
                    Commands.literal("devcreate")
                            .requires(source
                                    -> source.hasPermission(2)
                            )
                            .executes(context -> {
                                ServerPlayer creator
                                        = context.getSource()
                                                .getPlayerOrException();

                                /*
                                                * Create a unique dummy owner.
                                 */
                                String dummyName
                                        = "TeamTest"
                                        + (System.currentTimeMillis()
                                        % 10000);

                                FakePlayer dummyOwner
                                        = DummyPlayerManager.addDummy(
                                                creator,
                                                dummyName
                                        );

                                if (dummyOwner == null) {
                                    creator.sendSystemMessage(
                                            Component.literal(
                                                    "Failed to create test team owner."
                                            ).withStyle(
                                                    ChatFormatting.RED
                                            )
                                    );

                                    return 0;
                                }

                                /*
                                                * Assign a random Solo Heat.
                                                *
                                                * Keep it above 1 so the warning
                                                * is easier to test.
                                 */
                                int randomHeat
                                        = 10
                                        + new java.util.Random()
                                                .nextInt(
                                                        PlayerFloodData.MAX_HEAT
                                                        - 9
                                                );

                                PlayerFloodData.setSoloHeat(
                                        dummyOwner,
                                        randomHeat
                                );

                                /*
                                                * Random team name/color.
                                 */
                                String[] names = {
                                    "Nova",
                                    "Ember",
                                    "Vanguard",
                                    "Eclipse",
                                    "Tempest",
                                    "Apex",
                                    "Crimson"
                                };

                                String teamName
                                        = names[new java.util.Random()
                                                .nextInt(
                                                        names.length
                                                )];

                                net.minecraft.world.item.DyeColor[] colors
                                        = net.minecraft.world.item.DyeColor.values();

                                net.minecraft.world.item.DyeColor teamColor
                                        = colors[new java.util.Random()
                                                .nextInt(
                                                        colors.length
                                                )];

                                /*
                                                * Create the team with the dummy as owner.
                                 */
                                com.jushymaso222.theflood.team.FloodTeam testTeam
                                        = TeamManager.createTeam(
                                                dummyOwner,
                                                teamName,
                                                teamColor
                                        );

                                if (testTeam == null) {
                                    creator.sendSystemMessage(
                                            Component.literal(
                                                    "Failed to create dummy test team."
                                            ).withStyle(
                                                    ChatFormatting.RED
                                            )
                                    );

                                    DummyPlayerManager.removeDummy(
                                            creator.server,
                                            dummyName
                                    );

                                    return 0;
                                }

                                /*
                                                * Invite every REAL connected player.
                                                *
                                                * Do not invite additional dummies,
                                                * because they auto-accept and would
                                                * interfere with this warning test.
                                 */
                                int invitedCount = 0;

                                for (ServerPlayer player
                                        : creator.server
                                                .getPlayerList()
                                                .getPlayers()) {

                                    if (DummyPlayerManager.isDummy(
                                            player
                                    )) {
                                        continue;
                                    }

                                    /*
                                                        * Players already in teams cannot
                                                        * receive invitations.
                                     */
                                    if (TeamManager.isInTeam(
                                            player
                                    )) {
                                        continue;
                                    }

                                    boolean invited
                                            = TeamManager.invitePlayer(
                                                    dummyOwner,
                                                    player
                                            );

                                    if (invited) {
                                        invitedCount++;
                                    }
                                }

                                creator.sendSystemMessage(
                                        Component.literal(
                                                "Created test team "
                                                + teamName
                                                + " with owner Heat "
                                                + randomHeat
                                                + ". Invited "
                                                + invitedCount
                                                + " player(s)."
                                        ).withStyle(
                                                ChatFormatting.GREEN
                                        )
                                );

                                return 1;
                            })
            );
        }

        flood.then(
                team
        );

        LiteralArgumentBuilder<CommandSourceStack> elite =
                Commands.literal(
                        "elite"
                );

        elite.then(
                Commands.literal("make")
                        .requires(
                                source ->
                                        source.hasPermission(2)
                        )
                        .executes(context -> {

                        ServerPlayer player =
                                context.getSource()
                                        .getPlayerOrException();

                        Mob mob =
                                getLookedAtMob(
                                        player,
                                        32.0D
                                );

                        if (mob == null) {
                                player.sendSystemMessage(
                                        Component.literal(
                                                "Look at a mob within 32 blocks."
                                        ).withStyle(
                                                ChatFormatting.RED
                                        )
                                );

                                return 0;
                        }

                        int heat =
                                HeatManager.getEffectiveHeat(
                                        player
                                );

                        EliteManager.makeElite(
                                mob,
                                heat
                        );

                        EliteStateSync.syncBasic(
                                mob
                        );

                        player.sendSystemMessage(
                                Component.literal(
                                        "Converted "
                                                + mob.getName()
                                                        .getString()
                                                + " into an Elite."
                                ).withStyle(
                                        ChatFormatting.GREEN
                                )
                        );

                        return 1;
                })
                .then(
                        Commands.argument(
                                "spec",
                                StringArgumentType.greedyString()
                        )
                        .executes(context -> {

                        ServerPlayer player =
                                context.getSource()
                                        .getPlayerOrException();

                        Mob mob =
                                getLookedAtMob(
                                        player,
                                        32.0D
                                );

                        if (mob == null) {
                                player.sendSystemMessage(
                                        Component.literal(
                                                "Look at a mob within 32 blocks."
                                        ).withStyle(
                                                ChatFormatting.RED
                                        )
                                );

                                return 0;
                        }

                        String rawSpec =
                                StringArgumentType.getString(
                                        context,
                                        "spec"
                                );

                        EliteDebugSpec spec =
                                EliteDebugSpec.parse(
                                        rawSpec
                                );

                        if (spec == null) {
                                return 0;
                        }

                        String mutation =
                                spec.mutation() != null
                                        ? spec.mutation()
                                        : "none";

                        int heat =
                                HeatManager.getEffectiveHeat(
                                        player
                                );

                        EliteManager.makeElite(
                                mob,
                                heat,
                                mutation,
                                spec.attributes()
                        );

                        EliteStateSync.syncBasic(
                                mob
                        );

                        player.sendSystemMessage(
                                Component.literal(
                                        "Created Elite: "
                                                + mutation
                                                + " "
                                                + spec.attributes()
                                ).withStyle(
                                        ChatFormatting.GREEN
                                )
                        );

                        return 1;
                        })
                )
        );

        flood.then(
                elite
        );

        flood.then(
                Commands.literal(
                        "stats"
                )
                .requires(
                        source ->
                                source.hasPermission(
                                        2
                                )
                )
                .executes(
                        context ->
                                toggleStats(
                                        context.getSource()
                                )
                )
        );

        flood.then(
                Commands.literal(
                        "capability"
                )
                        .requires(
                                source ->
                                        source.hasPermission(
                                                2
                                        )
                        )
                        .executes(
                                context ->
                                        toggleCapability(
                                                context.getSource()
                                        )
                        )
        );

        event.getDispatcher()
                .register(
                        flood
                );
    }

    private static int toggleCapability(
                CommandSourceStack source
        ) throws CommandSyntaxException {

        ServerPlayer player =
                source.getPlayerOrException();


        /*
        * Already open -> close it.
        */
        if (
                CapabilityStatsWatch.isWatching(
                        player
                )
        ) {
                CapabilityStatsWatch.clear(
                        player
                );

                SyncCapabilityStatsPacket.sendClosed(
                        player
                );

                return 1;
        }


        /*
        * Otherwise open the inspector.
        */
        CapabilityStatsWatch.watch(
                player
        );

        SyncCapabilityStatsPacket.sendSnapshot(
                player
        );

        return 1;
        }

    private static int toggleStats(
                CommandSourceStack source
        ) throws CommandSyntaxException {

        ServerPlayer player =
                source.getPlayerOrException();

        Mob lookedAt =
                getLookedAtMob(
                        player,
                        32
                );

        /*
        * Looking at nothing = close inspector.
        */
        if (lookedAt == null) {
                EliteStatsWatch.clear(
                        player
                );

                SyncMobStatsPacket.sendClosed(
                        player
                );

                return 1;
        }

        /*
        * Looking at the mob we're already inspecting
        * toggles the inspector off.
        */
        if (
                EliteStatsWatch.isWatching(
                        player,
                        lookedAt
                )
        ) {
                EliteStatsWatch.clear(
                        player
                );

                SyncMobStatsPacket.sendClosed(
                        player
                );

                return 1;
        }

        /*
        * Otherwise begin inspecting this mob.
        */
        EliteStatsWatch.watch(
                player,
                lookedAt
        );

        SyncMobStatsPacket.sendSnapshot(
                player,
                lookedAt
        );

        return 1;
        }

    private static Mob getLookedAtMob(
            ServerPlayer player,
            double range
    ) {
        Vec3 eyePosition
                = player.getEyePosition();

        Vec3 lookVector
                = player.getViewVector(
                        1.0F
                );

        Vec3 endPosition
                = eyePosition.add(
                        lookVector.scale(
                                range
                        )
                );

        AABB searchBox
                = player.getBoundingBox()
                        .expandTowards(
                                lookVector.scale(
                                        range
                                )
                        )
                        .inflate(
                                1.0D
                        );

        EntityHitResult hit
                = ProjectileUtil.getEntityHitResult(
                        player,
                        eyePosition,
                        endPosition,
                        searchBox,
                        entity
                        -> entity instanceof Mob
                        && entity.isAlive(),
                        range * range
                );

        if (hit == null
                || !(hit.getEntity() instanceof Mob mob)) {
            return null;
        }

        return mob;
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
