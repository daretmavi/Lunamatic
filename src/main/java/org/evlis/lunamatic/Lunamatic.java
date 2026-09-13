package org.evlis.lunamatic;

import co.aikar.commands.PaperCommandManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.evlis.lunamatic.commands.LumaCommand;
import org.evlis.lunamatic.events.EntitySpawn;
import org.evlis.lunamatic.events.PlayerJoin;
import org.evlis.lunamatic.events.PlayerQuit;
import org.evlis.lunamatic.events.PlayerSleep;
import org.evlis.lunamatic.events.TimeSkip;
import org.evlis.lunamatic.triggers.Scheduler;
import org.evlis.lunamatic.utilities.LangManager;

import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;

public final class Lunamatic extends JavaPlugin {
    private static Lunamatic instance;
    public LangManager langManager;

    public TimeSkip timeSkip;
    public PlayerJoin playerJoin;
    public PlayerQuit playerQuit;
    public PlayerSleep playerSleep;
    public EntitySpawn entitySpawn;

    private final ComponentLogger logger = getComponentLogger();

    private static final String REQUIRED_VERSION = "26.2";

    private static final String GITHUB_RELEASE_API =
            "https://api.github.com/repos/daretmavi/Lunamatic/releases/latest";

    @Override
    public void onEnable() {
        // Begin Initialization
        logger.info("Begin plugin initialization...");

        // Assign instance variable
        instance = this;

        // Get versions
        String serverVersion = getServer().getMinecraftVersion();
        String currentVersion = this.getPluginMeta().getVersion();

        // Check server version, log error if not supported.
        if (!serverVersion.startsWith(REQUIRED_VERSION)) {
            logger.info(
                    "Unsupported server version detected! Expected ver: "
                            + REQUIRED_VERSION
                            + ", Your version: "
                            + serverVersion
            );
        }

        // Config Initialization
        saveDefaultConfig();
        loadGlobalConfig();

        // Build/update the Blood Moon visual datapack from config.yml.
        BloodMoonVisuals.configure(this);

        // Load translations
        LangManager.initialize(
                this,
                getDataFolder(),
                GlobalVars.lang
        );

        langManager = LangManager.getInstance();
        langManager.saveDefaultTranslations();
        langManager.loadTranslations();

        if (!langManager.doesTranslationExist(GlobalVars.lang)) {
            logger.info(
                    GlobalVars.lang
                            + " language does NOT exist! Falling back to the default language (en_US)."
            );

            GlobalVars.lang = "en_US";
            langManager.loadTranslations();
        }

        logger.info(
                langManager.getTranslation("lang_load_success")
        );

        // Update check
        if (GlobalVars.checkUpdates) {
            checkForUpdates(currentVersion);
        }

        // Class Initialization
        Scheduler schedule = new Scheduler();

        timeSkip = new TimeSkip();
        playerJoin = new PlayerJoin();
        playerQuit = new PlayerQuit();
        playerSleep = new PlayerSleep();
        entitySpawn = new EntitySpawn();

        // Defer moon state initialization until first server tick
        getServer().getGlobalRegionScheduler().runDelayed(this, (server) -> {
            GlobalVars.initializeWorldSettings();

            logger.info(
                    langManager.getTranslation("world_load_success")
                            + GlobalVars.currentMoonStateMap.keySet()
            );

            // Register commands here to avoid race conditions
            this.registerCommands();

            // Delay event registration to after world gen is complete
            Bukkit.getServer()
                    .getPluginManager()
                    .registerEvents(timeSkip, this);

            Bukkit.getServer()
                    .getPluginManager()
                    .registerEvents(playerJoin, this);

            Bukkit.getServer()
                    .getPluginManager()
                    .registerEvents(playerQuit, this);

            Bukkit.getServer()
                    .getPluginManager()
                    .registerEvents(playerSleep, this);

            Bukkit.getServer()
                    .getPluginManager()
                    .registerEvents(entitySpawn, this);
        }, 1L);

        schedule.StartMoonSchedule(this);

        // Notify of successful plugin start
        logger.info(
                langManager.getTranslation("plugin_success_load")
                        + currentVersion
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BloodMoonVisuals.resetAll(this);

        logger.info("Lunamatic has been disabled.");
    }

    public void registerCommands() {
        PaperCommandManager manager =
                new PaperCommandManager(this);

        manager.registerCommand(
                new LumaCommand(this)
        );
    }

    public void loadGlobalConfig() {
        try {
            // Set default values for missing keys
            getConfig().addDefault(
                    "checkForUpdates",
                    true
            );

            getConfig().addDefault(
                    "lang",
                    "en_US"
            );

            getConfig().addDefault(
                    "disabledWorlds",
                    new ArrayList<>()
            );

            getConfig().addDefault(
                    "fullMoonEnabled",
                    true
            );

            getConfig().addDefault(
                    "newMoonEnabled",
                    true
            );

            getConfig().addDefault(
                    "harvestMoonEnabled",
                    true
            );

            getConfig().addDefault(
                    "harvestMoonSpawnAllay",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonEnabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonSpawnVex",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonAllowSleep",
                    false
            );

            getConfig().addDefault(
                    "bloodMoonDieSides",
                    2
            );

            getConfig().addDefault(
                    "harvestMoonDieSides",
                    2
            );

            // Blood Moon visual defaults
            getConfig().addDefault(
                    "bloodMoonVisuals.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.ambientLight.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.ambientLight.color",
                    "#241010"
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.blockLight.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.blockLight.color",
                    "#fff2f2"
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.skyLight.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.skyLight.color",
                    "#9f1b22"
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.sky.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.sky.color",
                    "#720008"
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.fog.enabled",
                    true
            );

            getConfig().addDefault(
                    "bloodMoonVisuals.fog.color",
                    "#3a080d"
            );

            // Apply defaults if missing.
            // Existing user values are preserved.
            getConfig().options().copyDefaults(true);
            saveConfig();

            // Load values into GlobalVars
            GlobalVars.checkUpdates =
                    getConfig().getBoolean("checkForUpdates");

            GlobalVars.lang =
                    getConfig().getString("lang");

            GlobalVars.disabledWorlds =
                    getConfig().getStringList("disabledWorlds");

            GlobalVars.fullMoonEnabled =
                    getConfig().getBoolean("fullMoonEnabled");

            GlobalVars.newMoonEnabled =
                    getConfig().getBoolean("newMoonEnabled");

            GlobalVars.harvestMoonEnabled =
                    getConfig().getBoolean("harvestMoonEnabled");

            GlobalVars.harvestMoonSpawnAllay =
                    getConfig().getBoolean("harvestMoonSpawnAllay");

            GlobalVars.bloodMoonEnabled =
                    getConfig().getBoolean("bloodMoonEnabled");

            GlobalVars.bloodMoonSpawnVex =
                    getConfig().getBoolean("bloodMoonSpawnVex");

            GlobalVars.bloodMoonAllowSleep =
                    getConfig().getBoolean("bloodMoonAllowSleep");

            GlobalVars.bloodMoonDieSides =
                    getConfig().getInt("bloodMoonDieSides");

            GlobalVars.harvestMoonDieSides =
                    getConfig().getInt("harvestMoonDieSides");

        } catch (Exception e) {
            logger.info(
                    "Failed to load configuration! Disabling plugin. Error: "
                            + e.getMessage()
            );

            Bukkit.getPluginManager()
                    .disablePlugin(this);
        }
    }

    public void checkForUpdates(
            String currentVersionString
    ) {
        logger.info(
                langManager.getTranslation("update_check")
        );

        HttpURLConnection connection = null;

        try {
            URI uri =
                    URI.create(GITHUB_RELEASE_API);

            connection =
                    (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "Accept",
                    "application/vnd.github+json"
            );

            connection.setRequestProperty(
                    "X-GitHub-Api-Version",
                    "2022-11-28"
            );

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode =
                    connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.info(
                        langManager.getTranslation("update_error")
                );

                return;
            }

            try (InputStreamReader reader =
                         new InputStreamReader(
                                 connection.getInputStream()
                         )) {

                JsonObject release =
                        JsonParser
                                .parseReader(reader)
                                .getAsJsonObject();

                JsonElement tagElement =
                        release.get("tag_name");

                if (tagElement == null
                        || tagElement.isJsonNull()) {

                    logger.info(
                            langManager.getTranslation("update_error")
                    );

                    return;
                }

                String latestVersionString =
                        tagElement.getAsString();

                // Allow GitHub releases to use either:
                // 2.0.9-26.2
                // or:
                // v2.0.9-26.2
                if (latestVersionString.startsWith("v")) {
                    latestVersionString =
                            latestVersionString.substring(1);
                }

                ModuleDescriptor.Version latestVersion =
                        ModuleDescriptor.Version.parse(
                                latestVersionString
                        );

                ModuleDescriptor.Version currentVersion =
                        ModuleDescriptor.Version.parse(
                                currentVersionString
                        );

                if (currentVersion.compareTo(latestVersion) < 0) {
                    logger.info(
                            langManager
                                    .getTranslation("update_found")
                                    .replace(
                                            "%a",
                                            latestVersionString
                                    )
                                    .replace(
                                            "%b",
                                            currentVersionString
                                    )
                    );
                } else {
                    logger.info(
                            langManager.getTranslation(
                                    "up_to_date"
                            )
                    );
                }
            }

        } catch (Exception e) {
            logger.info(
                    langManager.getTranslation("update_error")
            );

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Lunamatic getInstance() {
        return instance;
    }
}