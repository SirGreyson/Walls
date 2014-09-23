/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.game;

import com.mchaze.walls.Walls;
import com.mchaze.walls.config.Settings;
import com.mchaze.walls.util.Messaging;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameTimer {

    private Walls plugin;
    private Game game;
    private GameStage gameStage;

    private BukkitTask gameTask;
    private int countdown;
    private int countdown2;

    public GameTimer(Walls plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.gameStage = GameStage.WAITING;
    }

    public void run() {
        if (gameStage == GameStage.STARTING || gameStage == GameStage.FORCE_STARTING) {
            this.countdown = Settings.STARTING_COUNTDOWN.asInt() + 1;
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (gameStage != GameStage.FORCE_STARTING && !game.canStart()) {
                        setGameStage(GameStage.WAITING);
                        Messaging.broadcast("&cThere are no longer enough Players to start the Game!");
                    } else {
                        countdown--;
                        if (countdown <= 0) game.startGame();
                        else if (countdown % 5 == 0)
                            Messaging.broadcast("&aGame starting in &b" + countdown + " &aseconds...");
                    }
                }
            }.runTaskTimer(plugin, 20, 20);

        } else if (gameStage == GameStage.RUNNING) {
            this.countdown = Settings.RUNNING_COUNTDOWN.asInt() + 1;
            this.countdown2 = Settings.WALL_COUNTDOWN.asInt();
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    countdown--;
                    countdown2--;
                    game.updateGameBoard();
                    if (countdown <= 0) game.finishGame();
                    if(countdown2 <= 0) game.doWallDrop();
                    else if(countdown2 % 30 == 0) Messaging.broadcast("&aWalls dropping in &e" + countdown2 + " &aseconds!");
                }
            }.runTaskTimer(plugin, 20, 20);

        } else if (gameStage == GameStage.FINISHING) {
            this.countdown = Settings.FINISHING_COUNTDOWN.asInt() + 1;
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    countdown--;
                    if (countdown <= 0) game.resetGame();
                    else if (countdown % 5 == 0) game.doFireworks();
                }
            }.runTaskTimer(plugin, 20, 20);
        }
    }

    private void cancel() {
        gameTask.cancel();
        gameTask = null;
    }

    public GameStage getGameStage() {
        return gameStage;
    }

    public void setGameStage(GameStage gameStage) {
        if (gameTask != null) cancel();
        this.gameStage = gameStage;
        this.countdown = 0;
        this.countdown2 = 0;
        if (gameStage.isRunnable()) run();
    }

    public int getCountdown() {
        return countdown;
    }
}
