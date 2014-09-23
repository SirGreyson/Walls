/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.game;

import com.mchaze.walls.Walls;
import com.mchaze.walls.arena.Arena;
import com.mchaze.walls.arena.ArenaManager;
import com.mchaze.walls.util.Messaging;

public class GameManager {

    private static GameManager instance = new GameManager();
    private Game activeGame;

    public static GameManager getInstance() {
        return instance;
    }

    public void loadGame(Walls plugin) {
        Arena activeArena = ArenaManager.getInstance().getRandomArena();
        if (activeArena == null) Messaging.printErr("No Arenas loaded! Plugin cannot run...");
        else {
            if(!ArenaManager.getInstance().loadArenaWorld(activeArena)) return;
            activeArena.loadSpawns();
            activeGame = new Game(plugin, activeArena);
            Messaging.printInfo("Game successfully loaded and ready to go!");
        }
    }

    public void unloadGame() {
        if (activeGame == null || activeGame.getCurrentArena() == null) return;
        activeGame.resetGame();
        Messaging.printInfo("Game unloaded... Restarting!");
    }

    public Game getGame() {
        return activeGame;
    }
}
