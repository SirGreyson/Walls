package com.mchaze.walls.game;/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

import com.mchaze.walls.config.Settings;
import com.mchaze.walls.util.Messaging;
import com.mchaze.walls.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class GameBoard {

    private Game game;
    private Scoreboard gameBoard;
    private Objective gameObj;

    private static Map<UUID, String> selectedTeams = new HashMap<UUID, String>();

    public GameBoard(Game game) {
        this.game = game;
        this.gameBoard = newBoard();
    }

    private Scoreboard newBoard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        gameObj = scoreboard.registerNewObjective("game", "dummy");
        gameObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        gameObj.setDisplayName(StringUtil.formatBoardTitle(game, Settings.GAME_BOARD_TITLE.asString()));
        registerTeams(scoreboard);
        return scoreboard;
    }

    private void registerTeams(Scoreboard scoreboard) {
        for (String teamID : Settings.getTeamIDs()) {
            Team team = scoreboard.registerNewTeam(teamID);
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setPrefix("§" + getTeamColor(teamID).getChar());
            team.setDisplayName(getTeamColor(teamID) + teamID + " Team");
        }
    }

    public void updateBoard() {
        gameObj.setDisplayName(StringUtil.formatBoardTitle(game, Settings.GAME_BOARD_TITLE.asString()));
        for (String score : gameBoard.getEntries()) gameBoard.resetScores(score);
        List<String> vars = StringUtil.formatBoardVars(game, Settings.GAME_BOARD_FORMAT.asList());
        for (int i = 0; i < vars.size(); i++) gameObj.getScore(vars.get(i)).setScore(i + 1);
    }

    public Set<Team> getTeams() {
        return gameBoard.getTeams();
    }

    public boolean isTeamFull(Team team) {
        return team.getSize() >= Settings.TEAM_MAX_PLAYERS.asInt();
    }

    public Team getTeam(String teamID) {
        return gameBoard.getTeam(teamID);
    }

    public Team getPlayerTeam(Player player) {
        return gameBoard.getPlayerTeam(player);
    }

    public static ChatColor getTeamColor(String teamID) {
        return ChatColor.valueOf(teamID);
    }

    public Team getSelectedTeam(Player player) {
        if (!selectedTeams.containsKey(player.getUniqueId())) return null;
        return gameBoard.getTeam(selectedTeams.get(player.getUniqueId()));
    }

    public static void selectTeam(Player player, Team team) {
        selectedTeams.put(player.getUniqueId(), team.getName());
        player.closeInventory();
        Messaging.send(player, "&aYou will now be on the " + team.getDisplayName());
    }

    public boolean hasWinningTeam() {
        int teamCount = 0;
        for (Team team : gameBoard.getTeams())
            if (team.getSize() > 0) teamCount++;
        return teamCount == 1;
    }

    public Team getLargestTeam() {
        Team largest = null;
        for (Team team : gameBoard.getTeams())
            if (largest == null || largest.getSize() < team.getSize()) largest = team;
        return largest;
    }

    private Team getSmallestTeam() {
        Team smallest = null;
        for (Team team : gameBoard.getTeams())
            if (smallest == null || smallest.getSize() > team.getSize()) smallest = team;
        return smallest;
    }

    public Team addPlayer(Player player) {
        Team team = getSelectedTeam(player);
        if (team != null && !isTeamFull(team)) team.addPlayer(player);
        else {
            if (team != null) Messaging.send(player, "&cYour selected Team is Full! Changing Team...");
            team = getSmallestTeam();
            if (isTeamFull(team)) Messaging.send(player, "&cAll Teams are Full! Spectating...");
            else team.addPlayer(player);
        }
        player.setScoreboard(gameBoard);
        return gameBoard.getPlayerTeam(player);
    }

    public void removePlayer(Player player) {
        Team team = gameBoard.getPlayerTeam(player);
        if (team != null) team.removePlayer(player);
    }
}
