/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.menu.menus;

import com.mchaze.walls.kit.KitManager;
import com.mchaze.walls.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class KitMenu extends Menu {

    private KitManager kitManager = KitManager.getInstance();
    private Map<Integer, String> commands = new HashMap<Integer, String>();

    public KitMenu(int menuSize, String menuTitle) {
        super(menuSize, menuTitle);
        int counter = 0;
        for (String kitID : kitManager.getKitIDs()) commands.put(counter++, "walls:kit select " + kitID);
    }

    @Override
    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, getSize(), getTitle());
        for (String kitID : kitManager.getKitIDs()) inv.addItem(kitManager.getKit(kitID).getMenuIcon(player));
        player.openInventory(inv);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (commands.containsKey(e.getRawSlot()))
            ((Player) e.getWhoClicked()).performCommand(commands.get(e.getRawSlot()));
    }
}
