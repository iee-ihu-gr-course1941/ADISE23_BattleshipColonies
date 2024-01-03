package com.example.battleshipsvag;

import com.example.battleshipsvag.GameRecordRepository;
import com.example.battleshipsvag.PlayerRepository;
import com.example.battleshipsvag.data.*;
import com.example.battleshipsvag.resources.AttackResultResource;
import com.example.battleshipsvag.resources.GameResource;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRecordRepository gameRecordRepository;
    private final PlayerRepository playerRepository;
    private volatile Game runningGame;
    public GameResource joinGame(String playerName) {
        Game game = findOrCreateGame();
        Player player = findOrCreatePlayer(playerName);

        GamePlayer gamePlayer = game.getGamePlayerOrNull(playerName);

        if (gamePlayer == null) {
            gamePlayer = new GamePlayer();
        }

        gamePlayer.setPlayerName(player.getPlayerName());
        game.addGamePlayer(gamePlayer);

        log.info("Player {} joined the current game", playerName);

        return getGameResourceForPlayer(playerName);
    }

    private Player findOrCreatePlayer(String playerName) {
        Player player = new Player();
        player.setPlayerName(playerName);
        playerRepository.findByPlayerName(playerName).ifPresentOrElse(p -> {
            log.info("Player {} already exists", playerName);
            player.setId(p.getId());
        }, () -> {
            log.info("Player {} does not exist, creating new player", playerName);
            playerRepository.save(player);
        });
        return player;
    }
    private Game findOrCreateGame() {

        if (runningGame == null) {
            synchronized (this) {
                if (runningGame == null) {
                    // find current game record from database or create a new one;
                    runningGame = gameRecordRepository.findCurrentNotFinishedGame()
                    // if there is no current game record, create a new one
                    .orElse(gameRecordRepository.save(new Game()));
                }
            }
        }

        return runningGame;
    }

    private Game findGame() {
        if (runningGame == null) {
            throw new IllegalStateException("No game is running");
        }

        return runningGame;
    }

    public GameResource getGameResourceForPlayer(String playerName) {
        Game game = findGame();

        GamePlayer player = game.getGamePlayer(playerName);

        GameResource gameResource = new GameResource();
        gameResource.setPlayerName(playerName);

        gameResource.setCanStartGame(player.isLeader() && game.bothPlayersPresent());

        gameResource.setStatus(game.getStatus());

        return gameResource;
    }


    public GameResource startGame(String playerName) {
        Game game = findGame();
        game.startGame(playerName);
        log.info("Player {} started the game", playerName);
        updateGame(game);

        return getGameResourceForPlayer(playerName);
    }

    public GameResource placeShip(String playerName, Map<ShipType, List<Integer>> shipPlacements) {
        Game game = findGame();

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        GamePlayer gamePlayer = game.getGamePlayer(playerName);

        if (gamePlayer.isHasPlacedShips()) {
            throw new IllegalStateException("Player has already placed their ships");
        }

        List<BoardCell> board = gamePlayer.getBoard();


        shipPlacements.forEach((key, value) -> {
            if (value == null) {
                throw new IllegalStateException("Ship placement is not valid");
            }

            value.forEach(i -> {
                if (i < 0 || i >= 64) {
                    throw new IllegalStateException("Ship placement is not valid");
                }
            });

        });

        game.placeShip(playerName, shipPlacements);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ShipType shipType = board.get(i * 8 + j).getShipType();

                sb.append("[").append(ShipType.NONE.equals(shipType) ? " " : shipType.name().charAt(0)).append("]");
            }
            sb.append("\n");
        }

        log.info("Player {} placed their ships on the board:\n{}", playerName, sb);

        updateGame(game);

        return getGameResourceForPlayer(playerName);
    }


    public AttackResultResource attack(String playerName, Integer attackPosition) {
        Game game = findGame();

        AttackResultResource attackResultResource = game.attack(playerName, attackPosition);

        if (attackResultResource.isMissed()) {
            log.info("Player {} missed", playerName);
        } else {
            log.info("Player {} hit a ship at {}", playerName, attackPosition);
            if (attackResultResource.isHasWon()) {
                log.info("Player {} won the game", playerName);
                runningGame = null;
            }
        }

        updateGame(game);

        return attackResultResource;
    }

    @Synchronized
    private void updateGame(Game game) {
        runningGame = gameRecordRepository.save(game);
    }


}
