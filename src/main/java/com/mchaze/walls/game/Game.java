package com.mchaze.walls.game;/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

import com.mchaze.walls.PlayerListener;
import com.mchaze.walls.Walls;
import com.mchaze.walls.arena.Arena;
import com.mchaze.walls.arena.ArenaManager;
import com.mchaze.walls.config.Settings;
import com.mchaze.walls.kit.KitManager;
import com.mchaze.walls.util.Messaging;
import me.lordal.haze.tokens.api.TokensAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Game {

    private Walls plugin;
    private Arena currentArena;
    private GameBoard gameBoard;
    private GameTimer gameTimer;

    private List<UUID> spectators = new ArrayList<UUID>();
    private Map<Location, UUID> protections = new HashMap<Location, UUID>();

    public Game(Walls plugin, Arena currentArena) {
        this.plugin = plugin;
        this.currentArena = currentArena;
        this.gameTimer = new GameTimer(plugin, this);
        this.gameBoard = new GameBoard(this);
    }

    public Arena getCurrentArena() {
        return currentArena;
    }

    public boolean isProtectedWall(Block block) {
        return currentArena.isWallZone(block.getLocation()) && gameTimer.getCountdown() <= Settings.RUNNING_COUNTDOWN.asInt() - Settings.WALL_COUNTDOWN.asInt();
    }

    public void updateGameBoard() {
        gameBoard.updateBoard();
    }

    public Team getTeam(String teamID) {
        return gameBoard.getTeam(teamID);
    }

    public Team getTeam(Player player) {
        return gameBoard.getPlayerTeam(player);
    }

    public int getTeamSize(String teamID) {
        return gameBoard.getTeam(teamID).getSize();
    }

    public GameStage getStage() {
        return gameTimer.getGameStage();
    }

    public void setStage(GameStage gameStage) {
        gameTimer.setGameStage(gameStage);
    }

    public int getCountdown() {
        return gameTimer.getCountdown();
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void setSpectator(Player player) {
        gameBoard.removePlayer(player);
        spectators.add(player.getUniqueId());
        player.teleport(currentArena.getWorld().getSpawnLocation());
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false));
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        Messaging.send(player, "&cYou are now spectating!");
    }

    public boolean isProtected(Block block) {
        return protections.containsKey(block.getLocation());
    }

    public boolean isOwner(Block block, Player player) { return protections.get(block.getLocation()).equals(player.getUniqueId()); }

    public Player getProtectionOwner(Block block) {
        return Bukkit.getPlayer(protections.get(block.getLocation()));
    }

    public void addProtection(Block block, Player player) {
        protections.put(block.getLocation(), player.getUniqueId());
        Messaging.send(player, "&aSuccessfully placed new protected &e" + block.getType());
    }

    public void removeProtection(Block block) {
        protections.remove(block.getLocation());
    }

    public int getPlayersNeeded() {
        return Settings.MIN_PLAYERS_TO_START.asInt() - Bukkit.getOnlinePlayers().length > 0 ? Settings.MIN_PLAYERS_TO_START.asInt() - Bukkit.getOnlinePlayers().length : 0;
    }

    public void addPlayer(Player player) {
        Team team = gameBoard.addPlayer(player);
        if (team == null) setSpectator(player);
        else {
            player.teleport(currentArena.getTeamSpawn(team.getName()));
            KitManager.getInstance().getSelectedKit(player).equipPlayer(player);
            Messaging.send(player, "&aYou have joined the " + team.getDisplayName());
        }
    }

    public void removePlayer(Player player) {
        gameBoard.removePlayer(player);
        if(spectators.contains(player)) spectators.remove(player);
        Iterator<Location> protIt = protections.keySet().iterator();
        while(protIt.hasNext())
            if(protections.get(protIt.next()).equals(player.getUniqueId())) protIt.remove();
        if (canFinish()) finishGame();
    }

    public boolean canStart() {
        return Bukkit.getOnlinePlayers().length >= Settings.MIN_PLAYERS_TO_START.asInt();
    }

    public void tryStart(boolean isForced) {
        setStage(isForced ? GameStage.FORCE_STARTING : GameStage.STARTING);
    }

    public void startGame() {
        setStage(GameStage.RUNNING);
        for (Player player : Bukkit.getOnlinePlayers()) addPlayer(player);
        Messaging.broadcast("&aWalls dropping in &e" + Settings.WALL_COUNTDOWN.asInt() + " &aseconds... Prepare for battle!");
    }

    public void doWallDrop() {
        currentArena.doWallDrop();
        Messaging.broadcast("&aThe Walls are coming down... Get ready to fight!");
    }

    public boolean canFinish() {
        return getStage() == GameStage.RUNNING && gameBoard.hasWinningTeam();
    }

    public void finishGame() {
        Team winningTeam = gameBoard.getLargestTeam();
        Messaging.broadcast("&aGame Over! " + winningTeam.getDisplayName() + " &awins!");
        Messaging.broadcast("&aServer will restart in &e" + Settings.FINISHING_COUNTDOWN.asInt() + " seconds&a!");
        setStage(GameStage.FINISHING);
        giveTokens(winningTeam);
    }

    private void giveTokens(Team team) {
        for(OfflinePlayer player : team.getPlayers()) {
            if(player.getPlayer() == null) continue;
            Messaging.send(player.getPlayer(), "&aYou have been rewarded &e" + Settings.WINNER_TOKENS.asInt() + " Tokens&a!");
            TokensAPI.depositTokens(player.getPlayer(), Settings.WINNER_TOKENS.asInt());
        }
    }

    public void doFireworks() {
        Team team = gameBoard.getLargestTeam();
        for (Location loc : currentArena.getTeamSpawns()) {
            Firework firework = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(DyeColor.valueOf(team.getName()).getFireworkColor()).trail(true).build());
            firework.setFireworkMeta(meta);
        }
    }

    public void resetGame() {
        setStage(GameStage.RESETTING);
        for (Player player : Bukkit.getOnlinePlayers()) PlayerListener.resetPlayer(player, true);
        ArenaManager.getInstance().unloadArenaWorld(currentArena);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Settings.RESET_COMMAND.toString());
    }
}
