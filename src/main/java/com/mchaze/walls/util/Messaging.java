/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.util;

import com.mchaze.walls.config.Settings;
import com.mchaze.walls.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;

import java.util.UUID;
import java.util.logging.Logger;

public class Messaging {

    private static String PREFIX;
    private static String BROADCAST_PREFIX;

    private static Logger log = Logger.getLogger("Walls");

    public static void send(CommandSender sender, String message) {
        if (PREFIX == null) PREFIX = Settings.MESSAGE_PREFIX.asString();
        sender.sendMessage(StringUtil.color(PREFIX + " " + message));
    }

    public static void broadcast(String message) {
        if (BROADCAST_PREFIX == null) BROADCAST_PREFIX = Settings.BROADCAST_PREFIX.asString();
        Bukkit.broadcastMessage(StringUtil.color(BROADCAST_PREFIX + " " + message));
    }

    public static void sendTeamChat(Team team, String message) {
        for(OfflinePlayer player : team.getPlayers())
            if(player.getPlayer() != null)
                player.getPlayer().sendMessage(StringUtil.color(ChatColor.valueOf(team.getName()) + "[" + team.getName() + "] " + player.getName() + "&7: " + message));
    }

    public static void sendGlobalChat(String playerName, String message) {
        Bukkit.broadcastMessage(StringUtil.color("&6[G] " + playerName + "&7: " + message));
    }

    public static void sendSpectatorChat(String playerName, String message) {
        for(UUID uuid : GameManager.getInstance().getGame().getSpectators())
            if(Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(StringUtil.color("&8[Spectator] " + playerName + ": &7" + message));
    }

    public static void printInfo(String message) {
        log.info("[Walls] " + ChatColor.YELLOW + message);
    }

    public static void printErr(String message) {
        log.severe("[Walls] " + ChatColor.RED + message);
    }
}
