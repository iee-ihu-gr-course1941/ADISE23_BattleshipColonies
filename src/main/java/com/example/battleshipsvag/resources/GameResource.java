package com.example.battleshipsvag.resources;

import com.example.battleshipsvag.data.BoardCell;
import com.example.battleshipsvag.data.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter @Getter
public class GameResource {
    private String playerName;
    private List<BoardCell> board = new ArrayList<>();
    private GameStatus status;
    private boolean canStartGame;
}
