package org.evlis.lunamatic.triggers;

import io.papermc.paper.world.MoonPhase;

import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.evlis.lunamatic.BloodMoonVisuals;
import org.evlis.lunamatic.GlobalVars;
import org.evlis.lunamatic.utilities.LangManager;
import org.evlis.lunamatic.utilities.players.PlayerMessage;
import org.evlis.lunamatic.utilities.worlds.ResetFlags;
import org.evlis.lunamatic.utilities.worlds.WorldUtils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Scheduler {

    private LangManager getTranslationManager() {
        return LangManager.getInstance();
    }

    @ApiStatus.Experimental
    public static void runGlobalDelayed(
            Plugin plugin,
            Runnable task,
            long delay
    ) {
        plugin.getServer()
                .getGlobalRegionScheduler()
                .runDelayed(
                        plugin,
                        t -> task.run(),
                        delay
                );
    }

    @ApiStatus.Experimental
    private void runWorldDelayed(
            Plugin plugin,
            World world,
            Runnable task,
            long delay
    ) {
        plugin.getServer()
                .getRegionScheduler()
                .runDelayed(
                        plugin,
                        world.getSpawnLocation(),
                        t -> task.run(),
                        delay
                );
    }

    public void StartMoonSchedule(Plugin plugin) {

        Random r = new Random();

        Logger logger = plugin.getLogger();

        plugin.getServer()
                .getGlobalRegionScheduler()
                .runAtFixedRate(
                        plugin,
                        task -> {

                            for (World world : Bukkit.getWorlds()) {

                                String worldName = world.getName();

                                if (!WorldUtils.isWorldEnabled(worldName)) {
                                    continue;
                                }

                                List<Player> playerList = world.getPlayers();

                                if (playerList.isEmpty()) {
                                    continue;
                                }

                                long time = world.getTime();

                                /*
                                 * Dawn.
                                 *
                                 * Reset all moon-event flags and restore
                                 * the Blood Moon environment.
                                 */
                                if (time >= 0 && time < 20) {

                                    if (GlobalVars.debug) {
                                        logger.info(
                                                getTranslationManager()
                                                        .getTranslation(
                                                                "sched_daydef_reset"
                                                        )
                                        );
                                    }

                                    ResetFlags.resetAll(
                                            worldName,
                                            plugin
                                    );

                                    ResetFlags.resetTickSpeed(world);

                                    @NotNull MoonPhase moonPhase =
                                            world.getMoonPhase();

                                    /*
                                     * Full Moon / Harvest Moon.
                                     */
                                    if (
                                            moonPhase == MoonPhase.FULL_MOON
                                                    &&
                                            GlobalVars.fullMoonEnabled
                                    ) {

                                        int chance = r.nextInt(
                                                GlobalVars.harvestMoonDieSides
                                        );

                                        if (
                                                chance == 0
                                                        &&
                                                GlobalVars.harvestMoonEnabled
                                        ) {

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setHarvestMoonToday(true);

                                            PlayerMessage.Send(
                                                    plugin,
                                                    playerList,
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "harvest_moon_tonight"
                                                            ),
                                                    NamedTextColor.GOLD
                                            );

                                        } else {

                                            PlayerMessage.Send(
                                                    plugin,
                                                    playerList,
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "full_moon_tonight"
                                                            ),
                                                    NamedTextColor.YELLOW
                                            );
                                        }

                                    /*
                                     * New Moon / Blood Moon.
                                     */
                                    } else if (
                                            moonPhase == MoonPhase.NEW_MOON
                                                    &&
                                            GlobalVars.newMoonEnabled
                                    ) {

                                        int chance = r.nextInt(
                                                GlobalVars.bloodMoonDieSides
                                        );

                                        if (
                                                chance == 0
                                                        &&
                                                GlobalVars.bloodMoonEnabled
                                        ) {

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setBloodMoonToday(true);

                                            PlayerMessage.Send(
                                                    plugin,
                                                    playerList,
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "blood_moon_tonight"
                                                            ),
                                                    NamedTextColor.DARK_RED
                                            );

                                        } else {

                                            PlayerMessage.Send(
                                                    plugin,
                                                    playerList,
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "new_moon_tonight"
                                                            ),
                                                    NamedTextColor.DARK_GRAY
                                            );
                                        }
                                    }
                                }

                                /*
                                 * Harvest Moon starts immediately after
                                 * sunset.
                                 */
                                if (time >= 12010 && time < 12030) {

                                    if (
                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .isHarvestMoonToday()
                                                    ||
                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .isHarvestMoonNow()
                                    ) {

                                        if (
                                                GlobalVars.currentMoonStateMap
                                                        .get(worldName)
                                                        .isHarvestMoonToday()
                                                        &&
                                                !GlobalVars.currentMoonStateMap
                                                        .get(worldName)
                                                        .isHarvestMoonNow()
                                        ) {

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setHarvestMoonNow(true);

                                            plugin.getServer()
                                                    .getScheduler()
                                                    .runTaskLater(
                                                            plugin,
                                                            () -> ResetFlags
                                                                    .resetTickSpeed(
                                                                            world
                                                                    ),
                                                            24000 - (int) time
                                                    );

                                            plugin.getServer()
                                                    .getScheduler()
                                                    .runTaskLater(
                                                            plugin,
                                                            () -> ResetFlags
                                                                    .resetAll(
                                                                            world.getName(),
                                                                            plugin
                                                                    ),
                                                            24000 - (int) time
                                                    );

                                            WorldUtils worldUtils =
                                                    new WorldUtils();

                                            worldUtils.setRandomTickSpeed(
                                                    world,
                                                    30
                                            );

                                            worldUtils.setClearSkies(
                                                    world,
                                                    24000 - (int) time
                                            );

                                            PlayerMessage.Send(
                                                    plugin,
                                                    playerList,
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "grass_growing"
                                                            ),
                                                    NamedTextColor.GOLD
                                            );

                                        } else {

                                            logger.warning(
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "sched_invalid_harv"
                                                            )
                                            );

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setHarvestMoonToday(false);

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setHarvestMoonNow(false);
                                        }
                                    }
                                }

                                /*
                                 * Start of night.
                                 *
                                 * The Blood Moon becomes active shortly
                                 * after sunset.
                                 */
                                if (time >= 12980 && time < 13000) {

                                    @NotNull MoonPhase moonPhase =
                                            world.getMoonPhase();

                                    /*
                                     * Existing NightEffects behaviour.
                                     */
                                    for (Player p : playerList) {

                                        NightEffects.ApplyMoonlight(
                                                plugin,
                                                p,
                                                moonPhase,
                                                24000 - (int) time
                                        );
                                    }

                                    /*
                                     * Blood Moon.
                                     */
                                    if (
                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .isBloodMoonToday()
                                                    ||
                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .isBloodMoonNow()
                                    ) {

                                        /*
                                         * First tick of the Blood Moon.
                                         */
                                        if (
                                                GlobalVars.currentMoonStateMap
                                                        .get(worldName)
                                                        .isBloodMoonToday()
                                                        &&
                                                !GlobalVars.currentMoonStateMap
                                                        .get(worldName)
                                                        .isBloodMoonNow()
                                        ) {

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setBloodMoonNow(true);

                                            /*
                                             * Activate the independent
                                             * Blood Moon World Clock.
                                             *
                                             * This does NOT change the
                                             * normal Minecraft Overworld
                                             * clock.
                                             */
                                            BloodMoonVisuals.start(
                                                    plugin,
                                                    world
                                            );

                                            /*
                                             * Reset everything at dawn.
                                             */
                                            plugin.getServer()
                                                    .getScheduler()
                                                    .runTaskLater(
                                                            plugin,
                                                            () -> ResetFlags
                                                                    .resetAll(
                                                                            world.getName(),
                                                                            plugin
                                                                    ),
                                                            24000 - (int) time
                                                    );

                                        } else {

                                            logger.warning(
                                                    getTranslationManager()
                                                            .getTranslation(
                                                                    "sched_invalid_blood"
                                                            )
                                            );

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setBloodMoonToday(false);

                                            GlobalVars.currentMoonStateMap
                                                    .get(worldName)
                                                    .setBloodMoonNow(false);

                                            /*
                                             * Make sure the visual state
                                             * is restored as well.
                                             */
                                            BloodMoonVisuals.stop(
                                                    plugin,
                                                    world
                                            );
                                        }
                                    }
                                }
                            }
                        },
                        1L,
                        20L
                );
    }
}
