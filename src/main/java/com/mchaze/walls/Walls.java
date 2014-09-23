package com.mchaze.walls;/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

import com.mchaze.walls.arena.ArenaManager;
import com.mchaze.walls.cmd.CommandManager;
import com.mchaze.walls.config.ConfigManager;
import com.mchaze.walls.game.GameManager;
import com.mchaze.walls.kit.KitManager;
import com.mchaze.walls.menu.MenuManager;
import com.mchaze.walls.util.Messaging;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Walls extends JavaPlugin {

    private CommandManager commandManager;
    private WorldEditPlugin worldEditPlugin;

    public void onEnable() {
        if (!registerHooks()) return;
        ConfigManager.getInstance().loadConfigs(this);
        ArenaManager.getInstance().loadArenas(this);
        KitManager.getInstance().loadKits();
        MenuManager.getInstance().loadMenus(this);
        GameManager.getInstance().loadGame(this);
        getCommandManager().registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        Messaging.printInfo("has been enabled");
    }

    public void onDisable() {
        ArenaManager.getInstance().saveArenas();
        ConfigManager.getInstance().saveConfigs();
        GameManager.getInstance().unloadGame();
        Messaging.printInfo("has been disabled");
    }

    private boolean registerHooks() {
        if (getWorldEdit() == null) Messaging.printErr("WorldEdit dependency not found... Disabling!");
        if (worldEditPlugin == null) getServer().getPluginManager().disablePlugin(this);
        return isEnabled();
    }

    public CommandManager getCommandManager() {
        if (commandManager == null) commandManager = new CommandManager(this);
        return commandManager;
    }

    public WorldEditPlugin getWorldEdit() {
        if (worldEditPlugin == null && getServer().getPluginManager().isPluginEnabled("WorldEdit"))
            worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        return worldEditPlugin;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        return getCommandManager().onCommand(sender, cmd, commandLabel, args);
    }
}
