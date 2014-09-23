/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.menu.menus;

import com.mchaze.walls.config.Settings;
import com.mchaze.walls.menu.Menu;
import com.mchaze.walls.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TeamMenu extends Menu {

    private Inventory menuInv;
    private Map<Integer, String> commands = new HashMap<Integer, String>();

    public TeamMenu(int menuSize, String menuTitle) {
        super(menuSize, menuTitle);
        int counter = -1;
        for (String teamID : Settings.getTeamIDs())
            commands.put(counter += 2, "team select " + teamID);
        reloadMenu();
    }

    private void reloadMenu() {
        if (menuInv == null) menuInv = Bukkit.createInventory(null, getSize(), getTitle());
        int counter = -1;
        for (String teamID : Settings.getTeamIDs())
            menuInv.setItem(counter += 2, getTeamIcon(teamID));
    }

    @Override
    public void openMenu(Player player) {
        player.openInventory(menuInv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (commands.containsKey(e.getRawSlot()))
            ((Player) e.getWhoClicked()).performCommand(commands.get(e.getRawSlot()));
    }

    private ItemStack getTeamIcon(String teamID) {
        ItemStack output = new ItemStack(Material.WOOL, 1, DyeColor.valueOf(teamID).getData());
        ItemMeta meta = output.getItemMeta();
        meta.setDisplayName(ChatColor.valueOf(teamID) + teamID + " Team");
        meta.setLore(StringUtil.colorAll(Arrays.asList("&aClick to join this Team!")));
        output.setItemMeta(meta);
        return output;
    }
}
