/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.arena;

import com.mchaze.walls.Walls;
import com.mchaze.walls.config.ConfigManager;
import com.mchaze.walls.util.FileUtil;
import com.mchaze.walls.util.Messaging;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class ArenaManager {

    private static ArenaManager instance = new ArenaManager();
    private static Walls plugin;
    private Map<String, Arena> loadedArenas = new TreeMap<String, Arena>(String.CASE_INSENSITIVE_ORDER);
    private Random random = new Random();

    public static ArenaManager getInstance() {
        return instance;
    }

    public void loadArenas(Walls plugin) {
        ArenaManager.plugin = plugin;
        YamlConfiguration c = ConfigManager.getInstance().getConfig("arenas");
        for (String arenaID : c.getKeys(false))
            loadedArenas.put(arenaID, Arena.deserialize(c.getConfigurationSection(arenaID)));
        Messaging.printInfo("Successfully loaded " + loadedArenas.size() + " Arena(s)!");
    }

    public void saveArenas() {
        YamlConfiguration c = ConfigManager.getInstance().getConfig("arenas");
        for (String arenaID : loadedArenas.keySet())
            c.set(arenaID, loadedArenas.get(arenaID).serialize());
        Messaging.printInfo("All Arenas have been saved..!");
    }

    public void createArena(String arenaID, String displayName, Location spawnLoc) {
        loadedArenas.put(arenaID, new Arena(arenaID, displayName, spawnLoc.getWorld().getName()));
    }

    public void removeArena(String arenaID) {
        ConfigManager.getInstance().getConfig("arenas").set(arenaID, null);
        loadedArenas.remove(arenaID);
    }

    public Collection<Arena> getArenas() {
        return loadedArenas.values();
    }

    public boolean arenaExists(String arenaID) {
        return loadedArenas.containsKey(arenaID);
    }

    public Arena getArena(String arenaID) {
        return loadedArenas.get(arenaID);
    }

    public Arena getRandomArena() {
        if (loadedArenas.size() <= 0) return null;
        return (Arena) loadedArenas.values().toArray()[random.nextInt(loadedArenas.size())];
    }

    private File getArenaTemplate(Arena arena) {
        File templateDir = new File(plugin.getDataFolder(), File.separator + "templates");
        if (!templateDir.exists()) templateDir.mkdir();
        File worldDir = FileUtil.getFromDir(templateDir, arena.getWorldName(), true);
        if (worldDir == null)
            Messaging.printErr("Error! Template for Arena [" + arena.getArenaID() + "] could not be found!");
        return worldDir;
    }

    public boolean loadArenaWorld(Arena arena) {
        Messaging.printInfo("Loading world template for Arena [" + arena.getArenaID() + "]...");
        File rootDir = plugin.getServer().getWorldContainer();
        File templateDir = getArenaTemplate(arena);
        if (rootDir == null || templateDir == null) return false;
        else if(!FileUtil.copy(templateDir, new File(rootDir, arena.getWorldName())))
            Messaging.printErr("Error! Template for Arena [" + arena.getArenaID() + "] could not be copied!");
        else plugin.getServer().createWorld(new WorldCreator(arena.getWorldName()));
        return arena.getWorld() != null; //TODO If successful, print message
    }

    public void unloadArenaWorld(Arena arena) {
        Messaging.printInfo("Unloading world template for Arena [" + arena.getArenaID() + "]...");
        plugin.getServer().unloadWorld(arena.getWorld(), false);
        File worldDir = new File(plugin.getServer().getWorldContainer(), arena.getWorldName());
        FileUtil.deleteDirectory(worldDir);
        Messaging.printInfo("World successfully unloaded and deleted!");
    }
}
