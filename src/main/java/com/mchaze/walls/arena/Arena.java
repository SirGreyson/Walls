/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.arena;

import com.mchaze.walls.config.ConfigManager;
import com.mchaze.walls.util.StringUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class Arena {

    private String arenaID;
    private String displayName;
    private String worldName;
    private Map<String, Location> teamSpawns;
    private Material wallBaseMaterial;
    private List<CuboidRegion> wallZones;

    public Arena(String arenaID, String displayName, String worldName) {
        this.arenaID = arenaID;
        this.displayName = displayName;
        this.worldName = worldName;
        this.teamSpawns = new TreeMap<String, Location>(String.CASE_INSENSITIVE_ORDER);
        this.wallBaseMaterial = Material.BEDROCK;
        this.wallZones = new ArrayList<CuboidRegion>();
    }

    public Arena(String arenaID, String displayName, String worldName, Material wallBaseMaterial, List<CuboidRegion> wallZones) {
        this.arenaID = arenaID;
        this.displayName = displayName;
        this.worldName = worldName;
        this.wallBaseMaterial = wallBaseMaterial;
        this.wallZones = wallZones;
    }

    public String getArenaID() {
        return arenaID;
    }

    public String getDisplayName(boolean doColor) {
        return doColor ? StringUtil.color(displayName) : displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Collection<Location> getTeamSpawns() {
        return teamSpawns.values();
    }

    public Location getTeamSpawn(String teamID) {
        return teamSpawns.get(teamID);
    }

    public void setTeamSpawn(String teamID, Location spawnLoc) {
        teamSpawns.put(teamID, spawnLoc);
    }

    public void doWallDrop() {
        for (CuboidRegion zone : wallZones) removeBlocks(wallBaseMaterial, zone);
    }

    private void removeBlocks(Material type, CuboidRegion reg) {
        Vector min = reg.getMinimumPoint();
        Vector max = reg.getMaximumPoint();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = getWorld().getBlockAt(x, y, z);
                    if (block.getType() != type) block.setTypeIdAndData(0, (byte) 0, false);
                }
    }

    public boolean isWallZone(Location loc) {
        for(CuboidRegion wallZone : wallZones)
            if(wallZone.contains(BukkitUtil.toVector(loc))) return true;
        return false;
    }

    public void addWallZone(CuboidRegion wallZone) {
        wallZones.add(wallZone);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("displayName", displayName);
        output.put("worldName", worldName);
        output.put("teamSpawns", serializeSpawns());
        output.put("wallBaseMaterial", wallBaseMaterial.toString());
        output.put("wallZones", StringUtil.parseRegions(wallZones));
        return output;
    }

    private Map<String, String> serializeSpawns() {
        Map<String, String> output = new HashMap<String, String>();
        for (String teamID : teamSpawns.keySet()) output.put(teamID, StringUtil.parseLoc(teamSpawns.get(teamID)));
        return output;
    }

    public static Arena deserialize(ConfigurationSection c) {
        return new Arena(c.getName(), c.getString("displayName"), c.getString("worldName"),
                Material.valueOf(c.getString("wallBaseMaterial")), StringUtil.parseRegionStrings(c.getStringList("wallZones")));
    }

    public void loadSpawns() {
        YamlConfiguration c = ConfigManager.getInstance().getConfig("arenas");
        Map<String, Location> output = new HashMap<String, Location>();
        for (String teamID : c.getConfigurationSection(arenaID + ".teamSpawns").getKeys(false))
            output.put(teamID, StringUtil.parseLocString(c.getString(arenaID + ".teamSpawns." + teamID)));
        this.teamSpawns = output;
    }
}
