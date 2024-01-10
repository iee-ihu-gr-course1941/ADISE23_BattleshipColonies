package com.example.battleshipsvag.data;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GamePlayerStatus {
    WAITING_FOR_PLAYER_TO_JOIN,
    WAITING_FOR_OPPONENT_TO_START_THE_GAME,
    CAN_START_GAME,
    BUILDING,
    WAITING_FOR_OPPONENT_BUILDING,
    ATTACKING,
    WAITING_FOR_OPPONENT_ATTACKING,
    FINISHED;

}