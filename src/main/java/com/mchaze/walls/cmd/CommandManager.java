/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.cmd;

import com.mchaze.walls.Walls;
import com.mchaze.walls.util.Messaging;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandManager {

    private Walls plugin;
    private CommandsManager<CommandSender> commands;

    public CommandManager(Walls plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String s) {
                return sender.isOp() || sender.hasPermission(s);
            }
        };
        CommandsManagerRegistration reg = new CommandsManagerRegistration(plugin, commands);
        reg.register(CommandManager.class);
        Messaging.printInfo("Commands successfully registered!");
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            Messaging.send(sender, "&cYou do not have permission to use this command!");
        } catch (MissingNestedCommandException e) {
            Messaging.send(sender, ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            Messaging.send(sender, ChatColor.RED + e.getMessage());
            Messaging.send(sender, ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                Messaging.send(sender, "&cNumber expected, string received instead.");
            } else {
                Messaging.send(sender, "&cAn error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            Messaging.send(sender, ChatColor.RED + e.getMessage());
        }
        return true;
    }

    @Command(aliases = {"arena"}, desc = "Arena management command")
    @NestedCommand(ArenaCommand.class)
    @CommandPermissions("walls.admin")
    public static void arenaCommand(CommandContext args, CommandSender sender) {
    }

    @Command(aliases = {"kit"}, desc = "Kit selection command")
    @NestedCommand(KitCommand.class)
    public static void kitCommand(CommandContext args, CommandSender sender) {
    }

    @Command(aliases = {"team"}, desc = "Team selection command")
    @NestedCommand(TeamCommand.class)
    public static void teamCommand(CommandContext args, CommandSender sender) {
    }

}
