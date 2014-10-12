/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls;

import com.mchaze.walls.config.Settings;
import com.mchaze.walls.game.Game;
import com.mchaze.walls.game.GameManager;
import com.mchaze.walls.game.GameStage;
import com.mchaze.walls.util.Messaging;
import com.mchaze.walls.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

public class PlayerListener implements Listener {

    private Walls plugin;
    private Game game = GameManager.getInstance().getGame();

    public PlayerListener(Walls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if(game.getStage() == GameStage.FINISHING || game.getStage() == GameStage.RESETTING) e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Game finishing... Try again soon");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        resetPlayer(e.getPlayer(), true);
        if(game.getStage() == GameStage.RUNNING) game.setSpectator(e.getPlayer());
        else if(game.getStage() == GameStage.WAITING && game.canStart()) game.tryStart(false);
        e.setJoinMessage(game.getStage() == GameStage.WAITING ? StringUtil.color("&b" + e.getPlayer().getName() + " &ehas joined the game! &c" + game.getPlayersNeeded() + " &emore players needed to start!") : null);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        resetPlayer(e.getPlayer(), true);
        game.removePlayer(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(game.getStage() != GameStage.RUNNING && !e.getPlayer().isOp()) e.setCancelled(true);
        else if(e.getBlock().getType() != Material.FURNACE && e.getBlock().getType() != Material.CHEST) return;
        else game.addProtection(e.getBlock(), e.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if(game.getStage() != GameStage.RUNNING && !e.getPlayer().isOp()) e.setCancelled(true);
        else if(game.isProtectedWall(e.getBlock())) e.setCancelled(true);
        else if(game.isProtected(e.getBlock())) {
            Messaging.send(e.getPlayer(), game.isOwner(e.getBlock(), e.getPlayer())
                    ? "&aSuccessfully removed your protected &e" + e.getBlock().getType()
                    : "&cThis &e" + e.getBlock().getType() + " &cbelongs to &e" + game.getProtectionOwner(e.getBlock()).getName());
            if(game.isOwner(e.getBlock(), e.getPlayer())) game.removeProtection(e.getBlock());
            else e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(game.isSpectator(e.getPlayer())) e.setCancelled(true);
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.FURNACE) return;
        else if(game.isProtected(e.getClickedBlock()) && !game.isOwner(e.getClickedBlock(), e.getPlayer())) {
            e.setCancelled(true);
            Messaging.send(e.getPlayer(), "&cThis &e" + e.getClickedBlock().getType() + " &cbelongs to &e" + game.getProtectionOwner(e.getClickedBlock()).getName());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player && game.getStage() != GameStage.RUNNING) e.setCancelled(true);
        else if(e.getEntity() instanceof Player && game.isSpectator((Player) e.getEntity())) e.setCancelled(true);
        else if(e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
            if(event.getDamager() instanceof Player && game.isSpectator((Player) event.getDamager())) e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Team team = game.getTeam(e.getEntity());
        Team team2 = game.getTeam(e.getEntity().getKiller());
        e.setDeathMessage(team == null || team2 == null ?
                (team != null ? StringUtil.color(ChatColor.valueOf(team.getName()) + e.getEntity().getName() + " died.") : null) :
                StringUtil.color(ChatColor.valueOf(team.getName()) + e.getEntity().getName() + " &bwas killed by " + ChatColor.valueOf(team2.getName()) + e.getEntity().getKiller().getName()));
        for(ItemStack i : e.getDrops())
            if(i != null && i.getType() != Material.AIR) e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), i);
        resetPlayer(e.getEntity(), false);
        game.setSpectator(e.getEntity());
        game.updateGameBoard();
        if(game.canFinish()) game.finishGame();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        if(game.isSpectator(e.getPlayer())) Messaging.sendSpectatorChat(e.getPlayer().getName(), e.getMessage());
        else if(game.getTeam(e.getPlayer()) != null && game.getStage() == GameStage.RUNNING) {
            Team team = game.getTeam(e.getPlayer());
            if(e.getMessage().startsWith("!"))
                Messaging.sendGlobalChat(ChatColor.valueOf(team.getName()) + "" + e.getPlayer().getName(), e.getMessage().replaceFirst("!", ""));
            else Messaging.sendTeamChat(team, e.getMessage());
        } else Messaging.sendChat(e.getPlayer(), e.getMessage());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(game.getStage() == GameStage.RUNNING && game.getCurrentArena().getWorldName().equalsIgnoreCase(e.getBlock().getWorld().getName()))
            for(Block block : e.getBlocks())
                if(game.isProtectedWall(block)) e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if(game.isSpectator(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onHungerChange(FoodLevelChangeEvent e) {
        if(game.getStage() != GameStage.RUNNING) e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPing(ServerListPingEvent e) {
        e.setMotd(StringUtil.color(game.getStage().asString()));
    }

    public static void resetPlayer(Player player, boolean doTeleport) {
        player.setHealth(player.getMaxHealth());
        player.setFlying(false);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        for(PotionEffect pe : player.getActivePotionEffects())
            player.removePotionEffect(pe.getType());
        player.setExp(0);
        player.setTotalExperience(0);
        if(doTeleport) player.teleport(Bukkit.getWorld(Settings.SPAWN_WORLD.asString()).getSpawnLocation());
    }
}
