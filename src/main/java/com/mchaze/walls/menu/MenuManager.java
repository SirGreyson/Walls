/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.menu;

import com.mchaze.walls.Walls;
import com.mchaze.walls.config.Settings;
import com.mchaze.walls.menu.menus.KitMenu;
import com.mchaze.walls.menu.menus.TeamMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.TreeMap;

public class MenuManager implements Listener {

    private static MenuManager instance = new MenuManager();
    private static Walls plugin;
    private Map<String, Menu> loadedMenus = new TreeMap<String, Menu>(String.CASE_INSENSITIVE_ORDER);

    public static MenuManager getInstance() {
        return instance;
    }

    public void loadMenus(Walls plugin) {
        MenuManager.plugin = plugin;
        loadedMenus.put(Settings.KIT_MENU_TITLE.asString(), new KitMenu(Settings.KIT_MENU_SIZE.asInt(), Settings.KIT_MENU_TITLE.asString()));
        loadedMenus.put(Settings.TEAM_MENU_TITLE.asString(), new TeamMenu(9, Settings.TEAM_MENU_TITLE.asString()));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public Menu getMenu(String menuID) {
        return loadedMenus.get(menuID);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        if (!loadedMenus.containsKey(e.getInventory().getTitle())) return;
        if (e.getCurrentItem() == null) return;
        e.setCancelled(true);
        loadedMenus.get(e.getInventory().getTitle()).handleClick(e);
    }
}
