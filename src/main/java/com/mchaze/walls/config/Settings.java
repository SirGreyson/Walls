/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.config;

import com.mchaze.walls.util.StringUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.List;

public enum Settings {

    MESSAGE_PREFIX,
    BROADCAST_PREFIX,

    SPAWN_WORLD,
    RESET_COMMAND,

    MIN_PLAYERS_TO_START,
    TEAM_MAX_PLAYERS,

    STARTING_COUNTDOWN,
    WALL_COUNTDOWN,
    RUNNING_COUNTDOWN,
    FINISHING_COUNTDOWN,

    KIT_MENU_TITLE,
    KIT_MENU_SIZE,
    TEAM_MENU_TITLE,

    GAME_BOARD_TITLE,
    GAME_BOARD_FORMAT;

    private YamlConfiguration c = ConfigManager.getInstance().getConfig("config");
    private static List<String> teamIDs = Arrays.asList("RED", "BLUE", "GREEN", "YELLOW");

    public String asString() {
        return StringUtil.color(c.getString(this.name()));
    }

    public int asInt() {
        return c.getInt(this.name());
    }

    public List<String> asList() {
        return StringUtil.colorAll(c.getStringList(this.name()));
    }

    public static List<String> getTeamIDs() {
        return teamIDs;
    }
}
