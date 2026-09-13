package org.evlis.lunamatic.utilities.worlds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import org.evlis.lunamatic.BloodMoonVisuals;
import org.evlis.lunamatic.GlobalVars;

public class ResetFlags {

    static WorldUtils worldUtils = new WorldUtils();

    /*
     * Backwards-compatible version used by the existing
     * PlayerJoin and LumaCommand code.
     */
    public static void resetAll(String worldName) {
        resetAll(worldName, null);
    }

    public static void resetAll(String worldName, Plugin plugin) {

        if (!GlobalVars.currentMoonStateMap.containsKey(worldName)) {
            return;
        }

        /*
         * Turn off the Blood Moon environment before clearing
         * the logical Blood Moon state.
         */
        World world = Bukkit.getWorld(worldName);

        if (world != null && plugin != null) {
            BloodMoonVisuals.stop(plugin, world);
        }

        GlobalVars.currentMoonStateMap
                .get(worldName)
                .setHarvestMoonToday(false);

        GlobalVars.currentMoonStateMap
                .get(worldName)
                .setHarvestMoonNow(false);

        GlobalVars.currentMoonStateMap
                .get(worldName)
                .setBloodMoonToday(false);

        GlobalVars.currentMoonStateMap
                .get(worldName)
                .setBloodMoonNow(false);
    }

    public static void resetTickSpeed(World world) {
        worldUtils.setRandomTickSpeed(world, 3);
    }
}