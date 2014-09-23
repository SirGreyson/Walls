/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.cmd;

import com.mchaze.walls.config.Settings;
import com.mchaze.walls.game.Game;
import com.mchaze.walls.game.GameBoard;
import com.mchaze.walls.game.GameManager;
import com.mchaze.walls.game.GameStage;
import com.mchaze.walls.menu.MenuManager;
import com.mchaze.walls.util.Messaging;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand {

    private static Game game = GameManager.getInstance().getGame();

    @Command(aliases = {"select"}, desc = "Team selection command", usage = "<ID>", min = 1, max = 1)
    public static void selectTeam(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player))
            Messaging.send(sender, "&cError! You cannot run this command from this console!");
        else if(game.getStage() == GameStage.RUNNING || game.getStage() == GameStage.FINISHING || game.getStage() == GameStage.RESETTING)
            Messaging.send(sender, "&cYou cannot use this command right now!");
        else if (!Settings.getTeamIDs().contains(args.getString(0)))
            Messaging.send(sender, "&cError, that is not a valid Team!");
        else GameBoard.selectTeam((Player) sender, GameManager.getInstance().getGame().getTeam(args.getString(0)));
    }

    @Command(aliases = {"menu"}, desc = "Team menu opening command", usage = "<ID>", max = 0)
    public static void openTeamMenu(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player))
            Messaging.send(sender, "&cError! You cannot run this command from this console!");
        else if(game.getStage() == GameStage.RUNNING || game.getStage() == GameStage.FINISHING || game.getStage() == GameStage.RESETTING)
            Messaging.send(sender, "&cYou cannot use this command right now!");
        else MenuManager.getInstance().getMenu(Settings.TEAM_MENU_TITLE.asString()).openMenu((Player) sender);
    }
}
