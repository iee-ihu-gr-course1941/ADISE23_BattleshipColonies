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
    private int score;
    private GamePlayerStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    private List<BoardCell> board = new ArrayList<>();

    public GamePlayer() {
        for (int i = 0; i < 64; i++) {
            board.add(new BoardCell(i));
        }
    }

}
