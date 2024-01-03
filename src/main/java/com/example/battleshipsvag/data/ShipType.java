package com.example.battleshipsvag.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public enum ShipType {
    NONE(0),
    CARRIER(5),
    BATTLESHIP(4),
    PATROL_BOAT(3),
    SUBMARINE(3),
    DESTROYER(2);

    final int size;
}
