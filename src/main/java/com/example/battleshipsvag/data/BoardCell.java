package com.example.battleshipsvag.data;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardCell extends AbstractEntity {
    private int boardIndex;
    private boolean isHit;
    private ShipType shipType = ShipType.NONE;
    public boolean hasNotNoneShipType() {
        return shipType != ShipType.NONE;
    }

    public BoardCell(int boardIndex) {
        this.boardIndex = boardIndex;
    }
}
