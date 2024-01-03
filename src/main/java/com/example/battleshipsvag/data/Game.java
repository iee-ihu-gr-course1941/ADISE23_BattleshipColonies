package com.example.battleshipsvag.data;


import com.example.battleshipsvag.resources.AttackResultResource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
@Getter @Setter
public class Game extends AbstractEntity {

    @OneToOne(cascade = CascadeType.ALL)
    private GamePlayer player1;

    @OneToOne(cascade = CascadeType.ALL)
    private GamePlayer player2;

    private String winner;

    private GameStatus status = GameStatus.NEW;

    public void addGamePlayer(GamePlayer player) {
        if (player1 == null) {
            player1 = player;
            player1.setLeader(true);
        } else if (player2 == null) {
            player2 = player;
        } else {
            throw new IllegalStateException("Game is full");
        }
    }

    public GamePlayer getGamePlayerOrNull(String playerName) {
        if (player1 != null && player1.getPlayerName().equals(playerName)) {
            return player1;
        } else if(player2 != null && player2.getPlayerName().equals(playerName)){
            return player2;
        }

        return null;
    }

    public GamePlayer getGamePlayer(String playerName) {
        GamePlayer player = getGamePlayerOrNull(playerName);
        if (player == null) {
            throw new IllegalStateException("Player not found");
        }

        return player;
    }

    public GamePlayer getOpponentPlayer(String playerName) {
        if (player1 != null && player1.getPlayerName().equals(playerName)) {
            return player2;
        } else if (player2 != null && player2.getPlayerName().equals(playerName)) {
            return player1;
        } else {
            throw new IllegalStateException("Player not found");
        }
    }

    public boolean playerBelongsToGame(String playerName) {
        return Optional.ofNullable(getGamePlayer(playerName)).isPresent();
    }

    public boolean bothPlayersPresent() {
        return player1 != null && player2 != null;
    }


    public boolean waitingForShipPlacement() {
        return player1 != null && player2 != null && !player1.isHasPlacedShips() && !player2.isHasPlacedShips();
    }

    public GamePlayer calculateWinner() {
        if (player1.getBoard().stream().filter(BoardCell::hasNotNoneShipType).allMatch(BoardCell::isHit)) {
            winner = player2.getPlayerName();
            return player2;
        } else if (player2.getBoard().stream().filter(BoardCell::hasNotNoneShipType).allMatch(BoardCell::isHit)) {
            winner = player1.getPlayerName();
            return player1;
        } else {
            return null;
        }
    }

    public boolean isPlayersTurn(String playerName) {
        if (getGamePlayer(playerName).isTurn()) {
            return true;
        }

        throw new IllegalStateException("It is not your turn");
    }

    public void startGame(String playerName) {
        if (status != GameStatus.NEW) {
            throw new IllegalStateException("Game has already started");
        }

        if (!bothPlayersPresent()) {
            throw new IllegalStateException("Both players must be present to start the game");
        }

        GamePlayer player = getGamePlayer(playerName);

        if (!player.isLeader()) {
            throw new IllegalStateException("Only player " + player1.getPlayerName() + " can start the game");
        }

        status = GameStatus.IN_PROGRESS;
        player1.setTurn(true);
        player2.setTurn(false);
    }

    public void placeShip(String playerName, Map<ShipType, List<Integer>> shipPlacements) {
        GamePlayer gamePlayer = getGamePlayer(playerName);
        List<BoardCell> board = gamePlayer.getBoard();
        shipPlacements.forEach((key, value) -> value.forEach(index -> board.get(index).setShipType(key)));
        gamePlayer.setHasPlacedShips(true);
    }

    public AttackResultResource attack(String playerName, Integer attackPosition) {
        if (GameStatus.IN_PROGRESS != getStatus()) {
            throw new IllegalStateException("Cannot Attack the game is not in progress");
        }

        if (waitingForShipPlacement()) {
            throw new IllegalStateException("Cannot Attack not all players have placed their ships");
        }

        GamePlayer attackingPlayer = getGamePlayer(playerName);

        GamePlayer opponentPlayer = getOpponentPlayer(playerName);
        BoardCell boardCell = opponentPlayer.getBoard().get(attackPosition);

        boardCell.setHit(true);
        boolean missed = boardCell.getShipType() == ShipType.NONE;
        AttackResultResource attackResultResource = new AttackResultResource();
        attackResultResource.setMissed(missed);

        if (!missed) {
            attackingPlayer.setScore(attackingPlayer.getScore() + boardCell.getShipType().getSize());
            GamePlayer winner = calculateWinner();

            if (winner != null) {
                setStatus(GameStatus.FINISHED);
                attackResultResource.setHasWon(winner.equals(attackingPlayer));
            }
        }

        return attackResultResource;
    }
}
