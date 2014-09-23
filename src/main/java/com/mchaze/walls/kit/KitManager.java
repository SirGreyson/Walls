/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.kit;

import com.mchaze.walls.config.ConfigManager;
import com.mchaze.walls.util.Messaging;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class KitManager {

    private static KitManager instance = new KitManager();
    private Map<String, Kit> loadedKits = new TreeMap<String, Kit>(String.CASE_INSENSITIVE_ORDER);
    private Map<UUID, String> selectedKits = new HashMap<UUID, String>();

    public static KitManager getInstance() {
        return instance;
    }

    public void loadKits() {
        YamlConfiguration c = ConfigManager.getInstance().getConfig("kits");
        for (String kitID : c.getKeys(false))
            loadedKits.put(kitID, Kit.deserialize(c.getConfigurationSection(kitID)));
        Messaging.printInfo("Successfully loaded " + loadedKits.size() + " Kit(s)!");
    }

    public boolean hasKit(String kitID) {
        return loadedKits.containsKey(kitID);
    }

    public Set<String> getKitIDs() {
        return loadedKits.keySet();
    }

    public Kit getKit(String kitID) {
        return loadedKits.get(kitID);
    }

    public Kit getSelectedKit(Player player) {
        if (!selectedKits.containsKey(player.getUniqueId())) return getKit("DEFAULT");
        return getKit(selectedKits.get(player.getUniqueId()));
    }

    public void tryKitSelect(Player player, Kit kit) {
        if (!kit.hasPermission(player)) Messaging.send(player, "&cYou do not have permission to equip this Kit!");
        else {
            selectedKits.put(player.getUniqueId(), kit.getKitID());
            player.closeInventory();
            Messaging.send(player, "&aKit " + kit.getDisplayName() + " &ahas been selected!");
        }
    }
}
