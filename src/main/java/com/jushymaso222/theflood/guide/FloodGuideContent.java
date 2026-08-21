package com.jushymaso222.theflood.guide;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class FloodGuideContent {

    private static final Map<
            FloodGuideCategory,
            List<FloodGuidePage>
    > PAGES =
            new EnumMap<>(
                    FloodGuideCategory.class
            );

    static {

        /*
         * =========================================================
         * GETTING STARTED
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.GETTING_STARTED,
                List.of(

                        new FloodGuidePage(
                                "Welcome to The Flood",
                                """
                                The Flood replaces several parts of normal
                                Minecraft survival with progression-based
                                systems.

                                Hostile mobs are controlled by The Flood's
                                spawning system, difficulty follows Heat,
                                teams share progression pressure, and special
                                hordes can attack players throughout a world.

                                Do not expect hostile spawning to behave like
                                vanilla Minecraft.
                                """
                        ),

                        new FloodGuidePage(
                                "The Basic Loop",
                                """
                                Your main progression value is Heat.

                                As your Heat rises:
                                - More enemy types become available.
                                - Hostile populations become larger.
                                - Enemies become more dangerous.
                                - Hordes become a greater threat.

                                Your current Effective Heat is displayed by
                                the flame icon on the HUD.
                                """
                        ),

                        new FloodGuidePage(
                                "HUD Overview",
                                """
                                The HUD shows several important pieces of
                                information.

                                Day Display:
                                Shows the current Flood day and time.

                                Heat Display:
                                Shows your current Effective Heat.

                                Team HUD:
                                When in a team, displays teammates, health,
                                distance, direction, and player colors.

                                Most Flood HUD elements can be repositioned
                                or hidden through Flood Settings.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * HEAT
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.HEAT,
                List.of(

                        new FloodGuidePage(
                                "What is Heat?",
                                """
                                Heat represents your progression and how much
                                danger The Flood assigns to you.

                                Heat ranges from 1 to 100.

                                Your own progression is called Solo Heat.
                                Other systems can temporarily or permanently
                                increase the Heat used by spawning and combat.

                                The final value used by most systems is called
                                Effective Heat.
                                """
                        ),

                        new FloodGuidePage(
                                "Effective Heat",
                                """
                                Effective Heat is the difficulty value The
                                Flood actually uses for you.

                                It is built from your Base Heat plus any
                                Proximity Heat bonus.

                                Base Heat is normally your Solo Heat.

                                If you are in a team, your Team Heat becomes
                                your Base Heat instead.
                                """
                        ),

                        new FloodGuidePage(
                                "Proximity Heat",
                                """
                                Nearby players increase local danger.

                                When other players are close to you, a
                                Proximity Heat bonus is added to your Base
                                Heat.

                                This means grouping together gives players
                                more cooperation and safety, but also attracts
                                greater pressure from The Flood.

                                Your current proximity bonus is shown beneath
                                the Heat icon when one is active.
                                """
                        ),

                        new FloodGuidePage(
                                "Heat and Combat",
                                """
                                Heat affects more than spawning.

                                Flood-controlled enemies scale relative to the
                                player fighting them. Higher-Heat players face
                                stronger versions of the same enemies.

                                Damage from players and damage received from
                                Flood enemies are adjusted using the player's
                                Effective Heat.

                                This allows players with different progression
                                levels to fight the same physical mob while
                                still experiencing appropriate difficulty.
                                """
                        )
                )
        );

        /*
        * =========================================================
        * ELITES
        * =========================================================
        */

        PAGES.put(
                FloodGuideCategory.ELITES,
                List.of(

                        new FloodGuidePage(
                                "What are Elites?",
                                """
                                Elites are enhanced Flood-controlled enemies that
                                begin appearing as Heat increases.

                                Every Elite has a Behavior that changes how it fights.
                                Elites may also possess Attributes that further modify
                                their abilities.

                                Higher Heat increases the threat posed by Elites and
                                allows more dangerous combinations to appear.

                                An Elite's Behavior and Attributes are displayed above
                                it once identified.
                                """
                        ),

                        new FloodGuidePage(
                                "Elite Behaviors",
                                """
                                Every Elite possesses one Behavior.

                                Behaviors fundamentally change how an enemy fights and
                                often introduce mechanics that must be learned and
                                countered.

                                Unlike Attributes, a Behavior is not simply a stat
                                increase.

                                Learning to recognize each Behavior is important as
                                Heat rises and Elite encounters become increasingly
                                dangerous.
                                """
                        ),

                        new FloodGuidePage(
                                "Frenzied",
                                """
                                Frenzied Elites specialize in aggressive close-range
                                combat.

                                They are extremely dangerous once allowed to maintain
                                pressure on their target.

                                Control their movement and avoid allowing them to stay
                                within striking distance for extended periods.
                                """
                        ),

                        new FloodGuidePage(
                                "Bulwark",
                                """
                                Bulwark Elites specialize in defense.

                                Their defenses can make direct attacks ineffective
                                until the Bulwark is staggered.

                                Watch its status indicator and exploit opportunities
                                when its defenses have been disrupted.
                                """
                        ),

                        new FloodGuidePage(
                                "Infested",
                                """
                                Infested Elites produce dangerous creatures during
                                combat.

                                These creatures can quickly turn a manageable fight
                                into an overwhelming one if ignored.

                                Controlling the additional enemies is often just as
                                important as fighting the Elite itself.
                                """
                        ),

                        new FloodGuidePage(
                                "Undying",
                                """
                                Undying Elites refuse to die normally.

                                When fatally wounded, they enter a revival state
                                instead of immediately dying.

                                Deal enough damage before the revival completes to
                                finish the Elite permanently.

                                Failed attempts weaken its ability to revive again.
                                """
                        ),

                        new FloodGuidePage(
                                "Commander",
                                """
                                Commander Elites coordinate nearby Flood-controlled
                                enemies against their target.

                                While supported by its forces, a Commander prefers to
                                remain away from direct combat and gains additional
                                protection.

                                Eliminating its support can make the Commander much
                                easier to confront.
                                """
                        ),

                        new FloodGuidePage(
                                "Spiked",
                                """
                                Spiked Elites alternate between defensive states that
                                punish different forms of attack.

                                Pay attention to its current state and adapt your
                                attacks accordingly.

                                Relying entirely on one type of weapon can make a
                                Spiked Elite considerably harder to kill.
                                """
                        ),

                        new FloodGuidePage(
                                "Pursuer",
                                """
                                Pursuer Elites specialize in relentless pursuit.

                                They can periodically gain extreme mobility to close
                                distance with fleeing targets.

                                Creating distance may provide temporary safety, but
                                do not expect a Pursuer to remain behind for long.
                                """
                        ),

                        new FloodGuidePage(
                                "Shifting",
                                """
                                Shifting Elites behave as though they are not entirely
                                anchored to normal space.

                                They can shift away from incoming projectiles and
                                periodically reposition behind their target.

                                Watch and listen for signs of an incoming shift.
                                Reacting quickly can prevent a devastating attack.
                                """
                        ),

                        new FloodGuidePage(
                                "████████",
                                """
                                CLASSIFICATION: ████████

                                Available information regarding this classification
                                is incomplete.

                                Reports indicate that some Flood entities may exhibit
                                behavior inconsistent with their apparent
                                classification.

                                IDENTIFICATION:
                                ███████████████████████

                                BEHAVIOR:
                                ███████████████████████

                                THREAT ASSESSMENT:
                                UNKNOWN
                                """
                        ),

                        new FloodGuidePage(
                                "Elite Attributes",
                                """
                                Attributes modify an Elite beyond its normal Behavior.

                                An Elite may possess multiple different Attributes.

                                Attributes can also appear at increased ranks:

                                ATTRIBUTE
                                ATTRIBUTE+
                                ATTRIBUTE++

                                Higher ranks strengthen the existing effect.

                                Standard Attributes primarily modify an enemy's normal
                                combat capabilities.

                                Special Attributes introduce entirely new mechanics
                                and are considerably rarer.
                                """
                        ),

                        new FloodGuidePage(
                                "Standard Attributes I",
                                """
                                Swift
                                Increased movement speed.

                                Tough
                                Increased maximum health.

                                Deadly
                                Increased attack damage.

                                Resilient
                                Increased defensive capabilities.

                                Hasty
                                Increased attack speed.

                                Reach
                                Increased attack reach.
                                """
                        ),

                        new FloodGuidePage(
                                "Standard Attributes II",
                                """
                                Leaping
                                Improved jumping and mobility.

                                Stalwart
                                Increased resistance to knockback.

                                Armored
                                Increased armor.

                                Regenerative
                                Regenerates health when given time to recover.

                                Fleet
                                Becomes faster while actively pursuing a target.

                                Brutal
                                Attacks cause increased knockback.
                                """
                        ),

                        new FloodGuidePage(
                                "Standard Attributes III",
                                """
                                Tenacious
                                Increased tracking and pursuit range.

                                Healthy
                                Increased maximum health.

                                Heavy
                                Gains additional durability at the cost of mobility.

                                Berserk
                                Becomes increasingly dangerous as its health falls.

                                Executioner
                                Deals increased damage to badly injured targets.

                                Vengeful
                                Temporarily becomes stronger after taking damage.

                                """
                        ),

                        new FloodGuidePage(
                                "Standard Attributes IV",
                                """
                                Adrenalized
                                Temporarily becomes faster after taking damage.

                                Unyielding
                                Large incoming attacks temporarily strengthen its
                                defenses.

                                Evasive
                                Improved ability to avoid attacks.

                                Climber
                                Improved ability to pursue players vertically.

                                Fireproof
                                Resistant to fire damage.

                                Blastproof
                                Resistant to explosion damage.
                                """
                        ),

                        new FloodGuidePage(
                                "Standard Attributes V",
                                """
                                Deflecting
                                Resistant to projectile damage.
                                """
                        ),

                        new FloodGuidePage(
                                "Special Attributes",
                                """
                                Special Attributes are rare modifiers that introduce
                                additional combat mechanics.

                                Their names are displayed differently from Standard
                                Attributes to warn players of the additional threat.

                                Special Attributes can also appear as + and ++
                                variants.

                                An Elite possessing a Special Attribute should not be
                                approached as though it were simply a stronger normal
                                enemy.
                                """
                        ),

                        new FloodGuidePage(
                                "Vampiric",
                                """
                                Successful attacks restore some of the Elite's health.

                                Higher ranks increase the strength of the healing.

                                Allowing a Vampiric Elite to repeatedly land attacks
                                can undo significant amounts of damage dealt to it.
                                """
                        ),

                        new FloodGuidePage(
                                "Radioactive",
                                """
                                Radioactive Elites emit a hazardous area around
                                themselves.

                                Remaining inside this area causes radiation exposure
                                to accumulate.

                                Exposure is initially harmless, but remaining within
                                the affected area for too long causes continuous
                                damage.

                                Leave the radioactive area to begin recovering.
                                """
                        ),

                        new FloodGuidePage(
                                "Volatile",
                                """
                                Damage dealt to a Volatile Elite causes instability to
                                build.

                                Once enough instability accumulates, the Elite
                                releases a damaging shockwave around itself.

                                Continuing to attack aggressively at close range can
                                therefore become extremely dangerous.
                                """
                        ),

                        new FloodGuidePage(
                                "Static",
                                """
                                Static Elites periodically charge nearby players with
                                unstable energy.

                                A targeted player receives a brief warning before the
                                charge discharges.

                                Escape the Elite's effective range before the charge
                                completes to avoid the resulting strike.
                                """
                        ),

                        new FloodGuidePage(
                                "Corrosive",
                                """
                                Successful attacks from a Corrosive Elite temporarily
                                reduce the effectiveness of the victim's armor.

                                Repeated attacks refresh the effect.

                                Heavily armored players should be especially careful,
                                as Corrosive Elites can temporarily undermine one of
                                their greatest defenses.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * TEAMS
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.TEAMS,
                List.of(

                        new FloodGuidePage(
                                "Teams",
                                """
                                Teams allow players to share progression and
                                use additional cooperative features.

                                Team members receive:
                                - Shared Team Heat
                                - Team name formatting
                                - Teammate HUD information
                                - Private team chat

                                The Teams tab is located on the left side of
                                your inventory.
                                """
                        ),

                        new FloodGuidePage(
                                "Team Heat",
                                """
                                A team's Heat begins with the highest Solo
                                Heat belonging to a team member.

                                A team-size bonus is then added as more players
                                join.

                                All members of the team use the resulting Team
                                Heat as their Base Heat.

                                Because of this, joining a veteran team can
                                dramatically increase your difficulty.
                                """
                        ),

                        new FloodGuidePage(
                                "Joining a Team",
                                """
                                When invited to a team, you can accept or
                                decline the invitation.

                                If joining would raise your Heat significantly,
                                The Flood warns you what your resulting Team
                                Heat will be before you confirm.

                                This prevents a low-Heat player from
                                accidentally jumping into high-level
                                progression without warning.
                                """
                        ),

                        new FloodGuidePage(
                                "Team Identity",
                                """
                                Team members display their team name alongside
                                their player identity.

                                The team's chosen color is used for the team
                                tag.

                                Players also receive a separate personal color
                                on the Team HUD. This color exists only to make
                                teammates easier to distinguish visually.
                                """
                        ),

                        new FloodGuidePage(
                                "The Team HUD",
                                """
                                The Team HUD displays up to four teammates at
                                once.

                                Each card shows:
                                - Player name
                                - Health
                                - Distance
                                - Direction arrow
                                - Personal HUD color

                                Teammates in other dimensions cannot provide a
                                useful direction or distance and are handled
                                accordingly.
                                """
                        ),

                        new FloodGuidePage(
                                "Team HUD Pages",
                                """
                                Large teams can contain more players than the
                                HUD displays at one time.

                                Use the page controls beneath the Team HUD
                                while your inventory is open to switch between
                                groups of teammates.

                                During normal gameplay, a page indicator shows
                                which manual page is currently selected.
                                """
                        ),

                        new FloodGuidePage(
                                "Nearest Players",
                                """
                                The Team HUD can automatically show the four
                                nearest teammates in your current dimension.

                                Activate this mode using the filter icon beneath
                                the Team HUD while your inventory is open.

                                If no teammates are in your dimension, the HUD
                                falls back to the selected manual page.
                                """
                        ),

                        new FloodGuidePage(
                                "Favorite Teammates",
                                """
                                You may favorite up to four teammates.

                                While your inventory is open, click a teammate
                                card to favorite or unfavorite that player.

                                Favorited names appear gold while managing the
                                HUD.

                                Use the Favorites filter to show your pinned
                                teammates.

                                Favorites are saved between game sessions, but
                                are cleared when you leave or disband a team.
                                """
                        ),

                        new FloodGuidePage(
                                "Team Chat",
                                """
                                Team members can switch between Global Chat and
                                Team Chat.

                                Global Chat:
                                Messages are visible normally.

                                Team Chat:
                                Messages are sent only to online members of
                                your current team.

                                The inventory chat icon can switch between the
                                two modes.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * SPAWNING
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.SPAWNING,
                List.of(

                        new FloodGuidePage(
                                "Custom Spawning",
                                """
                                The Flood uses its own hostile spawning system.

                                On servers where vanilla hostile spawning is
                                disabled by The Flood, normal Minecraft hostile
                                spawn behavior should not be expected.

                                The system tracks each player's Effective Heat
                                and maintains hostile pressure around that
                                player.
                                """
                        ),

                        new FloodGuidePage(
                                "Population Pressure",
                                """
                                Normal hostile spawning uses a target
                                population around each player.

                                As Effective Heat increases, the target
                                population grows.

                                Early Heat progression increases relatively
                                gently, while high Heat causes hostile
                                population pressure to accelerate much more
                                aggressively.
                                """
                        ),

                        new FloodGuidePage(
                                "Refilling Enemies",
                                """
                                The system does not simply attempt random
                                vanilla spawns every tick.

                                Instead, it compares the current Flood
                                population near you against your target
                                population.

                                When population falls below the target, new
                                enemies are periodically added to refill the
                                missing pressure.

                                Larger population deficits can cause several
                                enemies to spawn in one refill.
                                """
                        ),

                        new FloodGuidePage(
                                "Spawn Distance",
                                """
                                Flood-controlled enemies spawn within a
                                configured distance band around players.

                                They do not intentionally spawn directly on top
                                of you.

                                The server controls the minimum and maximum
                                allowed spawning distance.
                                """
                        ),

                        new FloodGuidePage(
                                "Lighting and Underground",
                                """
                                Flood spawning still considers environmental
                                conditions.

                                The server can limit the maximum block-light
                                level allowed for hostile spawning.

                                Flood mobs are ONLY affected by block-light, not 
                                daylight, so mobs can spawn throughout the day.

                                The default setting is 0 which means all
                                block-light, no matter how dim, stops mobs from
                                spawning. 

                                Underground players also receive modified
                                spawning pressure and refill timing, allowing
                                caves and surface play to be balanced
                                separately.
                                """
                        ),

                        new FloodGuidePage(
                                "Mob Unlocks",
                                """
                                Enemy types unlock according to Effective Heat,
                                not world day.

                                A player below an enemy's required Heat cannot
                                normally cause that enemy to enter their spawn
                                pool.

                                This means two players in the same world can
                                have different hostile progression depending on
                                their Heat.
                                """
                        ),

                        new FloodGuidePage(
                                "Existing Enemies",
                                """
                                Changing Heat does not transform enemies that
                                already exist.

                                A higher Heat changes the enemies that may be
                                selected for future spawns and changes combat
                                scaling relative to that player.

                                Older enemies remain the types they were when
                                they spawned.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * HORDES
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.HORDES,
                List.of(

                        new FloodGuidePage(
                                "Hordes",
                                """
                                Hordes are concentrated groups of
                                Flood-controlled enemies.

                                Unlike ambient hostile pressure, horde enemies
                                arrive together and are intended to create
                                sudden combat encounters rather than slowly
                                filling the surrounding area.
                                """
                        ),

                        new FloodGuidePage(
                                "Mini-Hordes",
                                """
                                Mini-hordes can occur outside Blood Moons.

                                Their occurrence and size are controlled by the
                                server and can scale as progression advances.

                                Horde mobs spawn in a clump near a selected
                                anchor rather than being scattered evenly
                                around the player.
                                """
                        ),

                        new FloodGuidePage(
                                "Horde Aggression",
                                """
                                Horde enemies are created as an active attack,
                                not passive background population.

                                They are assigned aggressive behavior toward
                                their intended player when spawned.

                                This makes a horde fundamentally different from
                                ordinary ambient enemies you may simply pass in
                                the world.
                                """
                        ),

                        new FloodGuidePage(
                                "Hordes and Population",
                                """
                                Horde enemies still count toward the hostile
                                pressure surrounding a player.

                                While you are already dealing with a horde,
                                ambient spawning does not simply replace every
                                enemy you kill with unrelated normal spawns.

                                This helps prevent multiple spawning systems
                                from stacking uncontrollably.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * BLOOD MOONS
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.BLOOD_MOONS,
                List.of(

                        new FloodGuidePage(
                                "Blood Moons",
                                """
                                Blood Moons are major nighttime horde events.

                                Unlike normal hostile progression, Blood Moon
                                occurrence is based on the world's day count.

                                When a Blood Moon begins, normal ambient
                                spawning gives way to the Blood Moon spawning
                                system.
                                """
                        ),

                        new FloodGuidePage(
                                "Blood Moon Waves",
                                """
                                Blood Moon enemies arrive in repeated waves.

                                The server controls:
                                - Time between waves
                                - Minimum wave size
                                - Maximum wave size
                                - Active enemy cap
                                - Total enemies allowed during the event

                                Later Blood Moons can increase both total enemy
                                pressure and the number of active enemies.
                                """
                        ),

                        new FloodGuidePage(
                                "Refill Behavior",
                                """
                                Blood Moon waves do not blindly spawn forever
                                at maximum population.

                                The system tracks the active Blood Moon
                                population.

                                When that population falls below a configured
                                fraction of the active cap, another wave may
                                refill the encounter.
                                """
                        ),

                        new FloodGuidePage(
                                "Blood Moon Enemies",
                                """
                                Blood Moon mobs are built for sustained pursuit.

                                They can receive increased follow range and are
                                immediately directed toward players.

                                Blood Moon pressure continues through the night
                                until the event ends.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * DAY / NIGHT
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.DAY_NIGHT,
                List.of(

                        new FloodGuidePage(
                                "Custom Day Cycle",
                                """
                                The Flood replaces the normal Minecraft
                                day/night pacing with configurable real-time
                                lengths.

                                Daytime and nighttime can therefore last much
                                longer or shorter than vanilla Minecraft.

                                The HUD displays the current Flood day and time
                                so you can plan around the custom cycle.
                                """
                        ),

                        new FloodGuidePage(
                                "Current Defaults",
                                """
                                Default server configuration:

                                Day Length:
                                30 real-world minutes

                                Night Length:
                                10 real-world minutes

                                Blood Moon Frequency:
                                Every 7 world days

                                Server owners may change these values.
                                The current server modified settings can
                                be viewed in the 'Server Settings' tab.
                                """
                        ),

                        new FloodGuidePage(
                                "Day vs Heat",
                                """
                                World Day and Heat serve different purposes.

                                World Day controls time-based world events such
                                as Blood Moon scheduling.

                                Heat controls individual progression such as
                                enemy unlocks, population pressure, and combat
                                difficulty.

                                A high world day does not automatically mean a
                                low-Heat player has unlocked every enemy.
                                """
                        )
                )
        );


        /*
         * =========================================================
         * COMMANDS
         * =========================================================
         */

        PAGES.put(
                FloodGuideCategory.COMMANDS,
                List.of(

                        new FloodGuidePage(
                                "Player Commands",
                                """
                                Most normal interaction with The Flood is done
                                through its GUI.

                                Some team and chat actions may also be
                                available through commands.

                                Development, dummy-player, testing, and debug
                                commands are intentionally not documented in
                                this guide.
                                """
                        ),

                        new FloodGuidePage(
                                "Team Chat Commands",
                                """
                                /team chat global

                                Switches your chat mode to Global Chat.


                                /team chat team

                                Switches your chat mode to private Team Chat.

                                You must belong to a team before Team Chat can
                                be enabled.
                                """
                        ),

                        new FloodGuidePage(
                                "Invite Confirmation",
                                """
                                Some team invitation confirmations use internal
                                commands when you click confirmation messages.

                                These commands are normally handled for you by
                                the interface and are not intended to be typed
                                manually.

                                If joining a team would greatly increase your
                                Heat, always read the warning before
                                confirming.
                                """
                        ),

                        new FloodGuidePage(
                                "Flood Settings",
                                """
                                Flood Settings can be opened from the pause
                                menu beside the Mods button.

                                Client-side HUD preferences can be adjusted
                                there without changing server progression.

                                Server gameplay configuration remains
                                controlled by the server.
                                """
                        )
                )
        );
    }

    private FloodGuideContent() {
    }

    public static List<FloodGuidePage> getPages(
            FloodGuideCategory category
    ) {
        return PAGES.getOrDefault(
                category,
                List.of()
        );
    }
}