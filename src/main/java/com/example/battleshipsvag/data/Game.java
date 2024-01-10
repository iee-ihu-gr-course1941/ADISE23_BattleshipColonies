package com.example.battleshipsvag.data;


import com.example.battleshipsvag.exceptions.GenericApiException;
import com.example.battleshipsvag.resources.AttackResult;
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
            player1.setStatus(GamePlayerStatus.WAITING_FOR_PLAYER_TO_JOIN);
        } else if (player2 == null) {
            player2 = player;
            player2.setStatus(GamePlayerStatus.WAITING_FOR_OPPONENT_TO_START_THE_GAME);
            player1.setStatus(GamePlayerStatus.CAN_START_GAME);
        } else {
            throw new GenericApiException("Game is full");
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
            throw new GenericApiException("Player not found");
        }

        return player;
    }

    public GamePlayer getOpponentPlayer(String playerName) {
        GamePlayer player = getOpponentPlayerOrNull(playerName);
        if (player == null) {
            throw new GenericApiException("Player not found");
        }

        return player;
    }

    public GamePlayer getOpponentPlayerOrNull(String playerName) {
        if (player1 != null && player1.getPlayerName().equals(playerName)) {
            return player2;
        } else if (player2 != null && player2.getPlayerName().equals(playerName)) {
            return player1;
        } else {
            return null;
        }
    }

    public boolean playerBelongsToGame(String playerName) {
        return Optional.ofNullable(getGamePlayer(playerName)).isPresent();
    }

    public boolean bothPlayersPresent() {
        return player1 != null && player2 != null;
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

    public void startGame(String playerName) {
        if (status != GameStatus.NEW) {
            throw new GenericApiException("Game has already started");
        }

        if (!bothPlayersPresent()) {
            throw new GenericApiException("Both players must be present to start the game");
        }

        GamePlayer player = getGamePlayer(playerName);

        if (!player.isLeader()) {
            throw new GenericApiException("Only player " + player1.getPlayerName() + " can start the game");
        }

        status = GameStatus.IN_PROGRESS;
        player1.setStatus(GamePlayerStatus.BUILDING);
        player2.setStatus(GamePlayerStatus.BUILDING);
    }

    public void placeShip(String playerName, Map<ShipType, List<Integer>> shipPlacements) {
        GamePlayer gamePlayer = getGamePlayer(playerName);
        List<BoardCell> board = gamePlayer.getBoard();
        shipPlacements.forEach((key, value) -> value.forEach(index -> board.get(index).setShipType(key)));

        GamePlayer opponentPlayer = getOpponentPlayer(playerName);
        if (opponentPlayer.getStatus() == GamePlayerStatus.WAITING_FOR_OPPONENT_BUILDING) {
            getPlayer1().setStatus(GamePlayerStatus.ATTACKING);
            getPlayer2().setStatus(GamePlayerStatus.WAITING_FOR_OPPONENT_ATTACKING);
        } else {
            gamePlayer.setStatus(GamePlayerStatus.WAITING_FOR_OPPONENT_BUILDING);
        }
    }

    public AttackResult attack(String playerName, Integer attackPosition) {
        if (GameStatus.IN_PROGRESS != getStatus()) {
            throw new GenericApiException("Cannot Attack the game is not in progress");
        }

        GamePlayer attackingPlayer = getGamePlayer(playerName);

        GamePlayer opponentPlayer = getOpponentPlayer(playerName);
        BoardCell boardCell = opponentPlayer.getBoard().get(attackPosition);

        boardCell.setHit(true);
        boolean missed = boardCell.getShipType() == ShipType.NONE;
        AttackResult attackResult = new AttackResult();
        attackResult.setMissed(missed);

        if (!missed) {
            attackingPlayer.setScore(attackingPlayer.getScore() + boardCell.getShipType().getSize());
            GamePlayer winner = calculateWinner();

            if (winner != null) {
                setStatus(GameStatus.FINISHED);
                attackResult.setHasWon(winner.equals(attackingPlayer));
                attackingPlayer.setStatus(GamePlayerStatus.FINISHED);
                opponentPlayer.setStatus(GamePlayerStatus.FINISHED);
            }
        } else {
            opponentPlayer.setStatus(GamePlayerStatus.ATTACKING);
            attackingPlayer.setStatus(GamePlayerStatus.WAITING_FOR_OPPONENT_ATTACKING);
        }

        return attackResult;
    }
}
