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
import java.util.logging.Logger;

public final class BloodMoonVisuals {

    private static final String CLOCK_ID = "lunamatic:blood_moon";

    /*
     * The timeline is periodic with 24000 ticks.
     *
     * 0      = normal environment
     * 12000  = strong Blood Moon environment
     */
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

    private BloodMoonVisuals() {
    }

    /**
     * Creates or updates the configurable Blood Moon visual datapack.
     */
    public static void configure(Plugin plugin) {
        Logger logger = plugin.getLogger();

        try {
            if (plugin.getServer().getWorlds().isEmpty()) {
                logger.warning(
                        "Cannot configure Blood Moon visuals because no worlds are loaded yet."
                );
                return;
            }

            Path worldDirectory =
                    plugin.getServer()
                            .getWorlds()
                            .get(0)
                            .getWorldFolder()
                            .toPath();

            Path datapackDirectory =
                    worldDirectory
                            .resolve("datapacks")
                            .resolve(DATAPACK_FOLDER);

            Files.createDirectories(datapackDirectory);

            writePackMetadata(datapackDirectory);
            writeTimeline(datapackDirectory, plugin);

            /*
             * The generated datapack is written directly into the world's
             * datapacks directory. Ask Minecraft to reload its data before
             * asking Paper's DatapackManager for the current pack list.
             *
             * This is intentionally done before refreshPacks().
             */
            plugin.getServer().reloadData();

            DatapackManager datapackManager =
                    plugin.getServer().getDatapackManager();

            datapackManager.refreshPacks();

            Datapack datapack =
                    findGeneratedDatapack(datapackManager);

            if (datapack != null) {
                if (!datapack.isEnabled()) {
                    datapack.setEnabled(true);
                }

                logger.info(
                        "Blood Moon visual datapack configured successfully."
                );

                return;
            }

            /*
             * Paper may not expose a runtime-created world datapack through
             * DatapackManager even though the generated files are already
             * present and the data reload has completed.
             *
             * The embedded Lunamatic plugin datapack contains the actual
             * Blood Moon timeline, so this is not considered a failure.
             */
            logger.info(
                    "Blood Moon visual datapack files were generated successfully."
            );

        } catch (Exception e) {
            logger.warning(
                    "Failed to configure Blood Moon visual datapack: "
                            + e.getMessage()
            );
        }
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

        Path packMeta =
                datapackDirectory.resolve("pack.mcmeta");

        String content =
                """
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

        Files.writeString(
                packMeta,
                content,
                StandardCharsets.UTF_8
        );
    }

    private static void writeTimeline(
            Path datapackDirectory,
            Plugin plugin
    ) throws IOException {

        Path timelineDirectory =
                datapackDirectory
                        .resolve("data")
                        .resolve("lunamatic")
                        .resolve("timeline");

        Files.createDirectories(timelineDirectory);

        Path timelineFile =
                timelineDirectory.resolve("blood_moon.json");

        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"clock\": \"lunamatic:blood_moon\",\n");
        json.append("  \"period_ticks\": 24000,\n");
        json.append("  \"tracks\": {");

        List<String> tracks = new ArrayList<>();

        if (isTrackEnabled(
                plugin,
                "bloodMoonVisuals.ambientLight.enabled"
        )) {
            String color = getColor(
                    plugin,
                    "bloodMoonVisuals.ambientLight.color",
                    DEFAULT_AMBIENT_LIGHT
            );

            tracks.add(
                    "\"minecraft:visual/ambient_light_color\": {"
                            + "\"keyframes\": ["
                            + "{\"ticks\": 0, \"value\": \"#0a0a0a\"},"
                            + "{\"ticks\": 1, \"value\": \"#120404\"},"
                            + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                            + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                            + "]}"
            );
        }

        if (isTrackEnabled(
                plugin,
                "bloodMoonVisuals.blockLight.enabled"
        )) {
            String color = getColor(
                    plugin,
                    "bloodMoonVisuals.blockLight.color",
                    DEFAULT_BLOCK_LIGHT
            );

            tracks.add(
                    "\"minecraft:visual/block_light_tint\": {"
                            + "\"keyframes\": ["
                            + "{\"ticks\": 0, \"value\": \"#ffffff\"},"
                            + "{\"ticks\": 1, \"value\": \"#fffafa\"},"
                            + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                            + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                            + "]}"
            );
        }

        if (isTrackEnabled(
                plugin,
                "bloodMoonVisuals.skyLight.enabled"
        )) {
            String color = getColor(
                    plugin,
                    "bloodMoonVisuals.skyLight.color",
                    DEFAULT_SKY_LIGHT
            );

            tracks.add(
                    "\"minecraft:visual/sky_light_color\": {"
                            + "\"keyframes\": ["
                            + "{\"ticks\": 0, \"value\": \"#ffffff\"},"
                            + "{\"ticks\": 1, \"value\": \"#ffb0b0\"},"
                            + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                            + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                            + "]}"
            );
        }

        if (isTrackEnabled(
                plugin,
                "bloodMoonVisuals.sky.enabled"
        )) {
            String color = getColor(
                    plugin,
                    "bloodMoonVisuals.sky.color",
                    DEFAULT_SKY
            );

            tracks.add(
                    "\"minecraft:visual/sky_color\": {"
                            + "\"keyframes\": ["
                            + "{\"ticks\": 0, \"value\": \"#78a7ff\"},"
                            + "{\"ticks\": 1, \"value\": \"#3d060b\"},"
                            + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                            + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                            + "]}"
            );
        }

        if (isTrackEnabled(
                plugin,
                "bloodMoonVisuals.fog.enabled"
        )) {
            String color = getColor(
                    plugin,
                    "bloodMoonVisuals.fog.color",
                    DEFAULT_FOG
            );

            tracks.add(
                    "\"minecraft:visual/fog_color\": {"
                            + "\"keyframes\": ["
                            + "{\"ticks\": 0, \"value\": \"#c0d8ff\"},"
                            + "{\"ticks\": 1, \"value\": \"#280509\"},"
                            + "{\"ticks\": 12000, \"value\": \"" + color + "\"},"
                            + "{\"ticks\": 23999, \"value\": \"" + color + "\"}"
                            + "]}"
            );
        }

        for (int i = 0; i < tracks.size(); i++) {
            json.append("\n    ");
            json.append(tracks.get(i));

            if (i < tracks.size() - 1) {
                json.append(",");
            }
        }

        json.append("\n  }\n");
        json.append("}\n");

        Files.writeString(
                timelineFile,
                json.toString(),
                StandardCharsets.UTF_8
        );
    }

    private static boolean isTrackEnabled(
            Plugin plugin,
            String path
    ) {
        if (!plugin.getConfig().getBoolean(
                "bloodMoonVisuals.enabled",
                true
        )) {
            return false;
        }

        return plugin.getConfig().getBoolean(path, true);
    }

    private static String getColor(
            Plugin plugin,
            String path,
            String fallback
    ) {
        String value =
                plugin.getConfig().getString(path, fallback);

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

    public static void start(
            Plugin plugin,
            World world
    ) {
        setClock(
                plugin,
                world,
                BLOOD_MOON_TICKS,
                true
        );
    }

    public static void stop(
            Plugin plugin,
            World world
    ) {
        setClock(
                plugin,
                world,
                NORMAL_TICKS,
                true
        );
    }

    private static void setClock(
            Plugin plugin,
            World world,
            long ticks,
            boolean pause
    ) {
        String dimension =
                world.getKey().toString();

        /*
         * /time operates on the clock belonging to the current
         * ServerLevel. "execute in <dimension>" gives the command
         * the requested world's command context.
         *
         * The custom clock is explicitly selected, so the normal
         * minecraft:overworld clock is never modified.
         */
        String command = String.format(
                "execute in %s run time of %s set %d",
                dimension,
                CLOCK_ID,
                ticks
        );

        boolean success =
                Bukkit.dispatchCommand(
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
            String pauseCommand =
                    String.format(
                            "execute in %s run time of %s pause",
                            dimension,
                            CLOCK_ID
                    );

            boolean pauseSuccess =
                    Bukkit.dispatchCommand(
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
            }
        }

        if (GlobalVars.debug) {
            Logger logger =
                    plugin.getLogger();

            logger.info(
                    "Blood Moon visual clock for world '"
                            + world.getName()
                            + "' set to "
                            + ticks
                            + " ticks."
            );
        }
    }

    public static void resetAll(
            Plugin plugin
    ) {
        for (World world : Bukkit.getWorlds()) {
            stop(plugin, world);
        }
    }
}