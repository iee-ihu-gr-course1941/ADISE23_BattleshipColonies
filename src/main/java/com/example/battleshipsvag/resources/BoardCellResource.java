package com.example.battleshipsvag.resources;

import com.example.battleshipsvag.data.ShipType;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class BoardCellResource {
    private boolean isHit;
    private ShipType shipType;
}
