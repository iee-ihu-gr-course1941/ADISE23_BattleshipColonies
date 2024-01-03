package com.example.battleshipsvag.resources;

import lombok.Data;

@Data
public class ScoredGameResource {
    private String gameId;
    private String player1Name;
    private int player1Score;
    private String player2Name;
    private int player2Score;
    private String winner;
}