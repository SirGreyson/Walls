/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.cmd;

import com.mchaze.walls.Walls;
import com.mchaze.walls.arena.Arena;
import com.mchaze.walls.arena.ArenaManager;
import com.mchaze.walls.config.Settings;
import com.mchaze.walls.game.GameManager;
import com.mchaze.walls.util.Messaging;
import com.mchaze.walls.util.StringUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand {

    private static Walls plugin = (Walls) Bukkit.getPluginManager().getPlugin("Walls");
    private static ArenaManager arenaManager = ArenaManager.getInstance();

    @Command(aliases = {"create"}, desc = "Arena creation command", usage = "<ID> [-d <displayName>]", flags = "d:", min = 1)
    public static void createArena(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if (arenaManager.arenaExists(args.getString(0)))
            Messaging.send(sender, "&cError! There is already an Arena with that ID!");
        else {
            arenaManager.createArena(args.getString(0), args.hasFlag('d') ? args.getFlag('d') : args.getString(0), ((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccessfully created new Arena with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"remove"}, desc = "Arena removal command", usage = "<ID>", min = 1, max = 1)
    public static void removeArena(CommandContext args, CommandSender sender) throws CommandException {
        if (!arenaManager.arenaExists(args.getString(0)))
            Messaging.send(sender, "&cError! There is no Arena with that ID!");
        else {
            arenaManager.removeArena(args.getString(0));
            Messaging.send(sender, "&aSuccessfully removed Arena with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"list"}, desc = "Arena listing command", max = 0)
    public static void listArenas(CommandContext args, CommandSender sender) throws CommandException {
        String arenaList = StringUtil.color("&7======= &cArenas &7=======");
        for (Arena arena : arenaManager.getArenas())
            arenaList += StringUtil.color("\n&8[" + arena.getDisplayName(true) + "&8]&7 " + arena.getArenaID());
        Messaging.send(sender, arenaList);
    }

    @Command(aliases = {"setspawn"}, desc = "Arena Team spawn setting command", usage = "<ID> <TeamID>", min = 2, max = 2)
    public static void setTeamSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if (!arenaManager.arenaExists(args.getString(0)))
            Messaging.send(sender, "&cError! There is no Arena with that ID!");
        else if (!Settings.getTeamIDs().contains(args.getString(1)))
            Messaging.send(sender, "&cError! Invalid Team ID!");
        else {
            arenaManager.getArena(args.getString(0)).setTeamSpawn(args.getString(1), ((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccessfully set spawn-point for Team &b" + args.getString(1) + " &ain Arena &b" + args.getString(0));
        }
    }

    @Command(aliases = {"addwallzone"}, desc = "Arena wall-zone addition command", usage = "<ID>", min = 1, max = 1)
    public static void addWallZone(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if (!arenaManager.arenaExists(args.getString(0)))
            Messaging.send(sender, "&cError! There is no Arena with that ID!");
        else if (plugin.getWorldEdit().getSelection((Player) sender) == null)
            Messaging.send(sender, "&cError! You haven't made a WorldEdit selection!");
        else {
            Selection sel = plugin.getWorldEdit().getSelection((Player) sender);
            arenaManager.getArena(args.getString(0)).addWallZone(new CuboidRegion(sel.getNativeMinimumPoint(), sel.getNativeMaximumPoint()));
            Messaging.send(sender, "&aWall zone added to Arena &b" + args.getString(0));
        }
    }

    @Command(aliases = {"forcestart"}, desc = "Arena force-starting command", max = 0)
    public static void startArena(CommandContext args, CommandSender sender) throws CommandException {
        GameManager.getInstance().getGame().tryStart(true);
    }
}
