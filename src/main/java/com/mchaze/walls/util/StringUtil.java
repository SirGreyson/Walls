/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.util;

import com.google.common.collect.Lists;
import com.mchaze.walls.game.Game;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static String asString(int input) {
        return String.valueOf(input);
    }

    private static int asInt(String input) {
        return Integer.parseInt(input);
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> colorAll(List<String> input) {
        List<String> output = new ArrayList<String>();
        for (String i : input) output.add(color(i));
        return output;
    }

    public static String formatTime(int time) {
        return String.valueOf(time / 60 + ":") + (String.valueOf(time % 60).length() == 1 ? "0" + String.valueOf(time % 60) : String.valueOf(time % 60));
    }

    public static String parseItem(ItemStack input) {
        String output = input.getType().name() + ":" + input.getAmount();
        if (input.hasItemMeta()) output += ":" + input.getItemMeta().getDisplayName();
        return output;
    }

    public static ItemStack parseItemString(String input) {
        String[] args = input.split(":");
        ItemStack output = new ItemStack(Material.valueOf(args[0]), asInt(args[1]));
        if (args.length == 2) return output;
        ItemMeta meta = output.getItemMeta();
        meta.setDisplayName(color(args[2]));
        List<String> lore = new ArrayList<String>();
        if (args.length >= 4)
            for(String loreLine : args[3].split("|")) lore.add(loreLine);
        meta.setLore(colorAll(lore));
        output.setItemMeta(meta);
        return output;
    }

    public static List<ItemStack> parseItemStrings(List<String> input) {
        List<ItemStack> output = new ArrayList<ItemStack>();
        for (String i : input) output.add(parseItemString(i));
        return output;
    }

    public static String parseLoc(Location input) {
        return input.getWorld().getName() + "," + input.getBlockX() + "," + input.getBlockY() + "," + input.getBlockZ();
    }

    public static Location parseLocString(String input) {
        String[] args = input.split(",");
        return new Location(Bukkit.getWorld(args[0]), asInt(args[1]), asInt(args[2]), asInt(args[3]));
    }

    public static String parseRegion(Region input) {
        return parseVector(input.getMinimumPoint()) + "/" + parseVector(input.getMaximumPoint());
    }

    public static List<String> parseRegions(List<CuboidRegion> input) {
        List<String> output = new ArrayList<String>();
        for (CuboidRegion i : input) output.add(parseRegion(i));
        return output;
    }

    public static CuboidRegion parseRegionString(String input) {
        String[] args = input.split("/");
        return new CuboidRegion(parseVectorString(args[0]), parseVectorString(args[1]));
    }

    public static List<CuboidRegion> parseRegionStrings(List<String> input) {
        List<CuboidRegion> output = new ArrayList<CuboidRegion>();
        for (String i : input) output.add(parseRegionString(i));
        return output;
    }

    public static String parseVector(Vector input) {
        return input.getBlockX() + "," + input.getBlockY() + "," + input.getBlockZ();
    }

    public static Vector parseVectorString(String input) {
        String[] args = input.split(",");
        return new Vector(asInt(args[0]), asInt(args[1]), asInt(args[2]));
    }

    public static String formatBoardTitle(Game game, String input) {
        return color(input.replace("%arena%", game.getCurrentArena().getDisplayName(true)).replace("%time%", formatTime(game.getCountdown())));
    }

    public static List<String> formatBoardVars(Game game, List<String> input) {
        List<String> output = new ArrayList<String>();
        for (String i : input)
            output.add(i.replace("%time%", formatTime(game.getCountdown()))
                    .replace("%redteam%", ChatColor.RED + "" + game.getTeamSize("RED"))
                    .replace("%blueteam%", ChatColor.BLUE + "" + game.getTeamSize("BLUE"))
                    .replace("%greenteam%", ChatColor.GREEN + "" + game.getTeamSize("GREEN"))
                    .replace("%yellowteam%", ChatColor.YELLOW + "" + game.getTeamSize("YELLOW")));
        return Lists.reverse(output);
    }
}
