package com.example.battleshipsvag;

import com.example.battleshipsvag.data.*;
import com.example.battleshipsvag.exceptions.GenericApiException;
import com.example.battleshipsvag.gen.DataGenerator;
import com.example.battleshipsvag.resources.AttackResult;
import com.example.battleshipsvag.resources.BoardCellResource;
import com.example.battleshipsvag.resources.GameResource;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
            if (GameStatus.FINISHED.equals(game.getStatus())) {
                throw new GenericApiException("Game is finished, please wait for a new game to start");
            }

            gamePlayer = new GamePlayer();
            gamePlayer.setPlayerName(player.getPlayerName());
            game.addGamePlayer(gamePlayer);
            updateGame(game);
        }

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
            throw new GenericApiException("No game is running");
        }

        return runningGame;
    }

    public List<BoardCellResource> getRandomShipPositions() {
        return DataGenerator.generateShipPlacement()
                .entrySet().stream()
                .map(entry -> entry.getValue().stream().map(index -> {
                    BoardCellResource boardCellResource = new BoardCellResource();
                    boardCellResource.setShipType(entry.getKey());
                    boardCellResource.setBoardIndex(index);
                    boardCellResource.setHit(false);
                    return boardCellResource;
                }).toList())
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(BoardCellResource::getBoardIndex))
                .collect(Collectors.toList());
    }

    public GameResource getGameResourceForPlayer(String playerName) {
        Game game = findGame();

        GamePlayer player = game.getGamePlayer(playerName);

        GameResource gameResource = new GameResource();
        gameResource.setPlayerName(playerName);

//        gameResource.setCanStartGame(player.isLeader() && game.bothPlayersPresent());

        gameResource.setStatus(player.getStatus());
        gameResource.setBoard(player.getBoard().stream().map(boardCell -> {
            BoardCellResource boardCellResource = new BoardCellResource();
            boardCellResource.setShipType(boardCell.getShipType());
            boardCellResource.setHit(boardCell.isHit());
            boardCellResource.setBoardIndex(boardCell.getBoardIndex());
            return boardCellResource;
        }).toList());

        Optional.ofNullable(game.getOpponentPlayerOrNull(playerName))
                .ifPresent(opponentPlayer ->
                        gameResource.setOpponentBoard(opponentPlayer.getBoard().stream().map(boardCell -> {
                                BoardCellResource boardCellResource = new BoardCellResource();
                                boardCellResource.setHit(boardCell.isHit());
                                if (boardCell.isHit()) {
                                    boardCellResource.setShipType(boardCell.getShipType());
                                }
                                boardCellResource.setBoardIndex(boardCell.getBoardIndex());
                                return boardCellResource;
                            }).toList()
                        )
                );
        gameResource.setVersion(String.valueOf(game.getVersion()));

        gameResource.setHasWon(playerName.equals(game.getWinner()));

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
            throw new GenericApiException("Game is not in progress");
        }

        GamePlayer gamePlayer = game.getGamePlayer(playerName);

        List<BoardCell> board = gamePlayer.getBoard();


        shipPlacements.forEach((key, value) -> {
            if (value == null) {
                throw new GenericApiException("Ship placement is not valid");
            }

            value.forEach(i -> {
                if (i < 0 || i >= 64) {
                    throw new GenericApiException("Ship placement is not valid");
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


    public GameResource attack(String playerName, Integer attackPosition) {
        Game game = findGame();

        AttackResult attackResult = game.attack(playerName, attackPosition);

        if (attackResult.isMissed()) {
            log.info("Player {} missed", playerName);
        } else {
            log.info("Player {} hit a ship at {}", playerName, attackPosition);
            if (attackResult.isHasWon()) {
                log.info("Player {} won the game", playerName);
                playerRepository.findByPlayerName(playerName).ifPresent(player -> {
                    player.setWins(player.getWins() + 1);
                    playerRepository.save(player);
                });
                try {
                    CompletableFuture.runAsync(() -> runningGame = null).get(10, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        updateGame(game);

        return getGameResourceForPlayer(playerName);
    }

    @Synchronized
    private void updateGame(Game game) {
        game.setVersion(game.getVersion() + 1);
        runningGame = gameRecordRepository.save(game);
    }


}
