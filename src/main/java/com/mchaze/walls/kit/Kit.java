/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.kit;

import com.mchaze.walls.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class Kit {

    private String kitID;
    private String permission;
    private String displayName;
    private Material iconMaterial;
    private List<ItemStack> kitItems;

    public Kit(String kitID, String permission, String displayName, Material iconMaterial, List<ItemStack> kitItems) {
        this.kitID = kitID;
        this.permission = permission;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.kitItems = kitItems;
    }

    public String getKitID() {
        return kitID;
    }

    public boolean hasPermission(Player player) {
        return permission.equalsIgnoreCase("none") || player.hasPermission(permission);
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getMenuIcon(Player player) {
        ItemStack output = new ItemStack(iconMaterial, 1);
        ItemMeta meta = output.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(StringUtil.colorAll(Arrays.asList(hasPermission(player) ? "&bClick to equip this Kit!" : "&cYou do not have permission for this Kit!")));
        output.setItemMeta(meta);
        return output;
    }

    public void equipPlayer(Player player) {
        player.getInventory().clear();
        for (ItemStack i : kitItems) player.getInventory().addItem(i);
    }

    public static Kit deserialize(ConfigurationSection c) {
        return new Kit(c.getName(), c.getString("permission"), StringUtil.color(c.getString("displayName")), Material.valueOf(c.getString("iconMaterial")), StringUtil.parseItemStrings(c.getStringList("kitItems")));
    }
}
