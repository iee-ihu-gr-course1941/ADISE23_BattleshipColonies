package com.example.battleshipsvag.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class GamePlayer extends AbstractEntity {
    private String playerName;
    private boolean leader;
    private boolean turn;
    private int score;

    @OneToMany(cascade = CascadeType.ALL)
    private List<BoardCell> board = new ArrayList<>();

    private boolean hasPlacedShips;
    public GamePlayer() {
        for (int i = 0; i < 64; i++) {
            board.add(new BoardCell(i));
        }
    }

}
