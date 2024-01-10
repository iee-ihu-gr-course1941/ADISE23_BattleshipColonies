package com.example.battleshipsvag.resources;

import com.example.battleshipsvag.data.GamePlayerStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter @Getter
public class GameResource {
    private String playerName;
    private List<BoardCellResource> board = new ArrayList<>();
    private List<BoardCellResource> opponentBoard = new ArrayList<>();
    private GamePlayerStatus status;
    private boolean hasWon;
    private String version;
}
