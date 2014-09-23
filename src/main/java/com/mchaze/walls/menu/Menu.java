/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.menu;

import com.mchaze.walls.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {

    private int menuSize;
    private String menuTitle;

    public Menu(int menuSize, String menuTitle) {
        this.menuSize = menuSize;
        this.menuTitle = StringUtil.color(menuTitle);
    }

    public int getSize() {
        return menuSize;
    }

    public String getTitle() {
        return menuTitle;
    }

    public boolean isMenuInv(Inventory inv) {
        return inv.getSize() == menuSize && inv.getTitle().equalsIgnoreCase(menuTitle);
    }

    public abstract void openMenu(Player player);

    public abstract void handleClick(InventoryClickEvent e);
}
