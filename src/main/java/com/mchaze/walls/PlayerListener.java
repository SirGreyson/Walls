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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        e.setJoinMessage(game.getStage() == GameStage.RUNNING ? StringUtil.color("&b" + e.getPlayer().getName() + " &ehas joined the game! &c" + game.getPlayersNeeded() + " &emore players needed to start!") : null);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        if(e.getPlayer().isOp()) return;
        resetPlayer(e.getPlayer(), true);
        game.removePlayer(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(game.getStage() != GameStage.RUNNING) e.setCancelled(true);
        else if(e.getBlock().getType() != Material.FURNACE && e.getBlock().getType() != Material.CHEST) return;
        game.addProtection(e.getBlock(), e.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if(game.getStage() != GameStage.RUNNING) e.setCancelled(true);
        else if(game.getCurrentArena().isWallZone(e.getBlock().getLocation())) e.setCancelled(true);
        else if(game.isProtected(e.getBlock()) && (e.getBlock().getType() == Material.FURNACE || e.getBlock().getType() == Material.CHEST)) {
            if(game.getProtectionOwner(e.getBlock()) == e.getPlayer()) {
                game.removeProtection(e.getBlock());
                Messaging.send(e.getPlayer(), "&aSuccessfully removed your protected &e" + e.getBlock().getType());
            } else {
                e.setCancelled(true);
                Messaging.send(e.getPlayer(), "&cThis &e" + e.getBlock().getType() + " &cbelongs to &e" + game.getProtectionOwner(e.getBlock()).getName());
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(game.isSpectator(e.getPlayer())) e.setCancelled(true);
        else if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        else if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.FURNACE) return;
        else if(game.isProtected(e.getClickedBlock()) && game.getProtectionOwner(e.getClickedBlock()) != e.getPlayer()) {
            e.setCancelled(true);
            Messaging.send(e.getPlayer(), "&cThis &e" + e.getClickedBlock().getType() + " &cbelongs to &e" + game.getProtectionOwner(e.getClickedBlock()).getName());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player && game.isSpectator((Player) e.getEntity())) e.setCancelled(true);
        else if(e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
            if(event.getDamager() instanceof Player && game.isSpectator((Player) event.getDamager())) e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Team team = game.getTeam(e.getEntity());
        Team team2 = game.getTeam(e.getEntity().getKiller());
        e.setDeathMessage(team == null || team2 == null ? null :
                ChatColor.valueOf(team.getName()) + e.getEntity().getName() + " &bwas killed by " + ChatColor.valueOf(team2.getName()) + e.getEntity().getKiller().getName());
        resetPlayer(e.getEntity(), false);
        game.setSpectator(e.getEntity());
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
        player.teleport(Bukkit.getWorld(Settings.SPAWN_WORLD.asString()).getSpawnLocation());
    }
}
