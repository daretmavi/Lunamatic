
package org.evlis.lunamatic;

import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class BloodMoonVisuals {

    private static final String CLOCK_ID = "lunamatic:blood_moon";

    private static final long NORMAL_TICKS = 0L;
    private static final long BLOOD_MOON_TICKS = 12000L;

    private static final String DATAPACK_FOLDER = "lunamatic-config";

    private static final String DEFAULT_AMBIENT_LIGHT = "#241010";
    private static final String DEFAULT_BLOCK_LIGHT = "#fff2f2";
    private static final String DEFAULT_SKY_LIGHT = "#9f1b22";
    private static final String DEFAULT_SKY = "#720008";
    private static final String DEFAULT_FOG = "#3a080d";

    private static final int PACK_FORMAT_MAJOR = 107;
    private static final int PACK_FORMAT_MINOR = 1;

    private record ClockState(long ticks, boolean paused) {}

    private static final Map<String, ClockState> CLOCK_STATES =
            new ConcurrentHashMap<>();

    private BloodMoonVisuals() {
    }

    /**
     * Vytvorí alebo aktualizuje datapack pre vizuálne efekty Blood Moon.
     */
    public static void configure(Plugin plugin) {
        Logger logger = plugin.getLogger();

        try {
            if (plugin.getServer().getWorlds().isEmpty()) {
                logger.warning(
                        "Cannot configure Blood Moon visuals because "
                                + "no worlds are loaded yet."
                );
                return;
            }

            Path worldDirectory = plugin.getServer()
                    .getWorlds()
                    .get(0)
                    .getWorldFolder()
                    .toPath();

            Path datapackDirectory = worldDirectory
                    .resolve("datapacks")
                    .resolve(DATAPACK_FOLDER);

            Files.createDirectories(datapackDirectory);

            writePackMetadata(datapackDirectory);
            writeTimeline(datapackDirectory, plugin);

            plugin.getServer().reloadData();

            DatapackManager datapackManager =
                    plugin.getServer().getDatapackManager();

            datapackManager.refreshPacks();

            Datapack datapack = findGeneratedDatapack(datapackManager);

            if (datapack != null && !datapack.isEnabled()) {
                datapack.setEnabled(true);
            }

            CLOCK_STATES.clear();
            resetAll(plugin);

            if (visualsEnabled(plugin)) {
                logger.info(
                        "Blood Moon visual datapack configured successfully."
                );
            } else {
                logger.info(
                        "Blood Moon visuals are disabled. "
                                + "The generated timeline contains no visual tracks."
                );
            }

        } catch (Exception e) {
            logger.warning(
                    "Failed to configure Blood Moon visual datapack: "
                            + e.getMessage()
            );
        }
    }

    private static boolean visualsEnabled(Plugin plugin) {
        return plugin.getConfig().getBoolean(
                "bloodMoonVisuals.enabled",
                true
        );
    }

    /**
     * Skontroluje, či je svet v konfigurácii disabledWorlds.
     * Používa presný názov sveta, napr. world_nether.
     */
    private static boolean isWorldDisabled(
            Plugin plugin,
            World world
    ) {
        return plugin.getConfig()
                .getStringList("disabledWorlds")
                .contains(world.getName());
    }

    private static Datapack findGeneratedDatapack(
            DatapackManager datapackManager
    ) {
        for (Datapack datapack : datapackManager.getPacks()) {
            String name = datapack.getName();

            if (name.equals(DATAPACK_FOLDER)
                    || name.endsWith(":" + DATAPACK_FOLDER)
                    || name.endsWith("/" + DATAPACK_FOLDER)) {
                return datapack;
            }
        }

        return null;
    }

    private static void writePackMetadata(
            Path datapackDirectory
    ) throws IOException {
        Path packMeta = datapackDirectory.resolve("pack.mcmeta");

        String content = """
                {
                  "pack": {
                    "description": "Lunamatic Blood Moon configured visuals",
                    "min_format": [%d, %d],
                    "max_format": [%d, %d]
                  }
                }
                """.formatted(
                PACK_FORMAT_MAJOR,
                PACK_FORMAT_MINOR,
                PACK_FORMAT_MAJOR,
                PACK_FORMAT_MINOR
        );

        Files.writeString(packMeta, content, StandardCharsets.UTF_8);
    }

    private static void writeTimeline(
            Path datapackDirectory,
            Plugin plugin
    ) throws IOException {
        Path timelineDirectory = datapackDirectory
                .resolve("data")
                .resolve("lunamatic")
                .resolve("timeline");

        Files.createDirectories(timelineDirectory);

        Path timelineFile =
                timelineDirectory.resolve("blood_moon.json");

        List<String> tracks = new ArrayList<>();

        if (visualsEnabled(plugin)) {
            addTrack(
                    tracks, plugin,
                    "bloodMoonVisuals.ambientLight",
                    "minecraft:visual/ambient_light_color",
                    DEFAULT_AMBIENT_LIGHT
            );

            addTrack(
                    tracks, plugin,
                    "bloodMoonVisuals.blockLight",
                    "minecraft:visual/block_light_tint",
                    DEFAULT_BLOCK_LIGHT
            );

            addTrack(
                    tracks, plugin,
                    "bloodMoonVisuals.skyLight",
                    "minecraft:visual/sky_light_color",
                    DEFAULT_SKY_LIGHT
            );

            addTrack(
                    tracks, plugin,
                    "bloodMoonVisuals.sky",
                    "minecraft:visual/sky_color",
                    DEFAULT_SKY
            );

            addTrack(
                    tracks, plugin,
                    "bloodMoonVisuals.fog",
                    "minecraft:visual/fog_color",
                    DEFAULT_FOG
            );
        }

        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"clock\": \"")
                .append(CLOCK_ID)
                .append("\",\n");
        json.append("  \"period_ticks\": 24000,\n");
        json.append("  \"tracks\": {");

        for (int i = 0; i < tracks.size(); i++) {
            json.append("\n    ").append(tracks.get(i));

            if (i < tracks.size() - 1) {
                json.append(",");
            }
        }

        if (!tracks.isEmpty()) {
            json.append("\n  ");
        }

        json.append("}\n");
        json.append("}\n");

        Files.writeString(
                timelineFile,
                json.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static void addTrack(
            List<String> tracks,
            Plugin plugin,
            String configPrefix,
            String trackId,
            String fallbackColor
    ) {
        if (!plugin.getConfig().getBoolean(
                configPrefix + ".enabled", true)) {
            return;
        }

        String color = getColor(
                plugin,
                configPrefix + ".color",
                fallbackColor
        );

        String track = "\"" + trackId + "\": {"
                + "\"modifier\": \"multiply\","
                + "\"keyframes\": ["
                + "{\"ticks\": 0, \"value\": \"#ffffff\"},"
                + "{\"ticks\": 1, \"value\": \"#ffffff\"},"
                + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                + "]"
                + "}";

        tracks.add(track);
    }

    private static String getColor(
            Plugin plugin,
            String path,
            String fallback
    ) {
        String value = plugin.getConfig().getString(path, fallback);

        if (value == null) {
            return fallback;
        }

        value = value.trim();

        if (!value.matches("#[0-9a-fA-F]{6}")) {
            plugin.getLogger().warning(
                    "Invalid Blood Moon color '"
                            + value
                            + "' at config path '"
                            + path
                            + "'. Using default "
                            + fallback
                            + "."
            );

            return fallback;
        }

        return value.toLowerCase();
    }

    public static void start(Plugin plugin, World world) {
        if (isWorldDisabled(plugin, world)) {
            if (GlobalVars.debug) {
                plugin.getLogger().info(
                        "Blood Moon visuals skipped for disabled world '"
                                + world.getName() + "'."
                );
            }
            return;
        }

        if (!visualsEnabled(plugin)) {
            stop(plugin, world);
            return;
        }

        setClock(plugin, world, BLOOD_MOON_TICKS, true);
    }

    public static void stop(Plugin plugin, World world) {
        if (isWorldDisabled(plugin, world)) {
            if (GlobalVars.debug) {
                plugin.getLogger().info(
                        "Blood Moon clock reset skipped for disabled world '"
                                + world.getName() + "'."
                );
            }
            return;
        }

        setClock(plugin, world, NORMAL_TICKS, true);
    }

    private static void setClock(
            Plugin plugin,
            World world,
            long ticks,
            boolean pause
    ) {
        String dimension = world.getKey().toString();

        ClockState desiredState = new ClockState(ticks, pause);
        ClockState previousState = CLOCK_STATES.get(dimension);

        if (desiredState.equals(previousState)) {
            if (GlobalVars.debug) {
                plugin.getLogger().info(
                        "Blood Moon clock for world '"
                                + world.getName()
                                + "' is already configured as "
                                + ticks
                                + " ticks, paused="
                                + pause
                                + ". Skipping commands."
                );
            }
            return;
        }

        String command = String.format(
                "execute in %s run time of %s set %d",
                dimension,
                CLOCK_ID,
                ticks
        );

        boolean success = Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command
        );

        if (!success) {
            plugin.getLogger().warning(
                    "Failed to set Blood Moon clock for world '"
                            + world.getName()
                            + "'. Command: "
                            + command
            );
            return;
        }

        if (pause) {
            String pauseCommand = String.format(
                    "execute in %s run time of %s pause",
                    dimension,
                    CLOCK_ID
            );

            boolean pauseSuccess = Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    pauseCommand
            );

            if (!pauseSuccess) {
                plugin.getLogger().warning(
                        "Failed to pause Blood Moon clock for world '"
                                + world.getName()
                                + "'. Command: "
                                + pauseCommand
                );
                return;
            }
        }

        CLOCK_STATES.put(dimension, desiredState);

        if (GlobalVars.debug) {
            plugin.getLogger().info(
                    "Blood Moon visual clock for world '"
                            + world.getName()
                            + "' set to "
                            + ticks
                            + " ticks, paused="
                            + pause
                            + "."
            );
        }
    }

    public static void resetAll(Plugin plugin) {
        for (World world : Bukkit.getWorlds()) {
            if (isWorldDisabled(plugin, world)) {
                if (GlobalVars.debug) {
                    plugin.getLogger().info(
                            "Blood Moon reset skipped for disabled world '"
                                    + world.getName()
                                    + "'."
                    );
                }
                continue;
            }

            if (GlobalVars.debug) {
                plugin.getLogger().info(
                        "Resetting Blood Moon clock for world '"
                                + world.getName()
                                + "' (" + world.getKey() + ")."
                );
            }

            stop(plugin, world);
        }
    }
}
