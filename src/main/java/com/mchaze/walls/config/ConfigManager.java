/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.config;

import com.mchaze.walls.Walls;
import com.mchaze.walls.util.FileUtil;
import com.mchaze.walls.util.Messaging;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ConfigManager {

    private static ConfigManager instance = new ConfigManager();
    private static Walls plugin;
    private Map<String, YamlConfiguration> loadedConfigs = new TreeMap<String, YamlConfiguration>(String.CASE_INSENSITIVE_ORDER);

    public static ConfigManager getInstance() {
        return instance;
    }

    public void loadConfigs(Walls plugin) {
        ConfigManager.plugin = plugin;
        loadConfig("config");
        loadConfig("arenas");
        loadConfig("kits");
    }

    public void saveConfigs() {
        saveConfig("arenas");
    }

    private boolean isConfigLoaded(String fileName) {
        return loadedConfigs.containsKey(fileName);
    }

    public YamlConfiguration getConfig(String fileName) {
        return loadedConfigs.get(fileName);
    }

    private void loadConfig(String fileName) {
        File cFile = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!cFile.exists() && plugin.getResource(fileName + ".yml") != null)
            plugin.saveResource(fileName + ".yml", false);
        else FileUtil.validate(cFile, false);
        if (!isConfigLoaded(fileName)) loadedConfigs.put(fileName, YamlConfiguration.loadConfiguration(cFile));
    }

    private void saveConfig(String fileName) {
        if (!isConfigLoaded(fileName))
            Messaging.printErr("Error! Tried to save non-existent File [" + fileName + ".yml]");
        else if (!saveFile(fileName)) Messaging.printErr("Error! Could not save File [" + fileName + ".yml]");
    }

    private boolean saveFile(String fileName) {
        File cFile = new File(plugin.getDataFolder(), fileName + ".yml");
        try {
            loadedConfigs.get(fileName).save(cFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
