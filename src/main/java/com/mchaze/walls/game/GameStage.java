/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.game;

public enum GameStage {

    WAITING, STARTING, FORCE_STARTING, RUNNING, FINISHING, RESETTING;

    public boolean isRunnable() {
        return this == STARTING || this == FORCE_STARTING || this == RUNNING || this == FINISHING;
    }

    public String asString() {
        if(this == WAITING || this == STARTING || this == FORCE_STARTING) return "&aLOBBY";
        else if(this == RUNNING) return "&cIN GAME";
        else return "&eFINISHING";
    }
}
