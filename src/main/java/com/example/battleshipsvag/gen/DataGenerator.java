package com.example.battleshipsvag.gen;

import com.example.battleshipsvag.GameRecordRepository;
import com.example.battleshipsvag.PlayerRepository;
import com.example.battleshipsvag.data.*;
import com.github.javafaker.Faker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGenerator {

    private final GameRecordRepository gameRecordRepository;
    private final Faker faker = new Faker();
    private final PlayerRepository playerRepository;
    public void generate() {
        gameRecordRepository.deleteAll();
        List<Game> games = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            games.add(new Game());
        }

        games.forEach(game -> {
            GamePlayer gamePlayer1 = new GamePlayer();
            String character = faker.lordOfTheRings().character();
            gamePlayer1.setPlayerName(character);
            game.addGamePlayer(gamePlayer1);

            String character2 = faker.lordOfTheRings().character();
            while (character2.equals(character)) {
                character2 = faker.lordOfTheRings().character();
            }
            GamePlayer gamePlayer2 = new GamePlayer();
            gamePlayer2.setPlayerName(character2);
            game.addGamePlayer(gamePlayer2);

            game.startGame(character);

            Map<ShipType, List<Integer>> shipPlacement = generateShipPlacement();
            game.placeShip(character, shipPlacement);

            Map<ShipType, List<Integer>> shipPlacement2 = generateShipPlacement();
            game.placeShip(character2, shipPlacement2);
            // while this messes with the logic it skips the searching of the player on each attack
            doBattle(gamePlayer1, gamePlayer2);
            game.calculateWinner();
            game.setStatus(GameStatus.FINISHED);

            if (playerRepository.findByPlayerName(character).isEmpty()){
                Player player = new Player();
                player.setPlayerName(character);
                playerRepository.save(player);
            }

            if (playerRepository.findByPlayerName(character2).isEmpty()){
                Player player = new Player();
                player.setPlayerName(character2);
                playerRepository.save(player);
            }

            playerRepository.findByPlayerName(game.getWinner()).ifPresent(player -> {
                player.setWins(player.getWins() + 1);
                playerRepository.save(player);
            });
        });

        log.info("Saving generated games to database - started");
        gameRecordRepository.saveAll(games);
        log.info("Saving generated games to database - done");
    }

    private void doBattle(GamePlayer player1, GamePlayer player2) {

        List<Integer> player1Indexes = new ArrayList<>();
        List<BoardCell> player1BoardCells = player1.getBoard();

        List<Integer> player2Indexes = new ArrayList<>();
        List<BoardCell> player2BoardCells = player2.getBoard();

        int scoreToWin = Stream.of(ShipType.values()).mapToInt(ShipType::getSize).sum();
        int p1Score = 0;
        int p2Score = 0;;

        boolean p1Turn = true;
        List<Integer> attackIndexes = player1Indexes;
        List<BoardCell> defendingBoard = player2BoardCells;

        while (true) {
            int randomIndex = faker.random().nextInt(0, 63);

            while (attackIndexes.contains(randomIndex)) {
                randomIndex = faker.random().nextInt(0, 63);
            }

            BoardCell boardCell = defendingBoard.get(randomIndex);
            boardCell.setHit(true);

            attackIndexes.add(randomIndex);
            if (boardCell.getShipType() != ShipType.NONE) {
                if (p1Turn) {
                    p1Score++;
                } else {
                    p2Score++;
                }
                if (p1Score == scoreToWin || p2Score == scoreToWin) {
                    break;
                }
            } else {
                p1Turn = !p1Turn;
                if (p1Turn) {
                    attackIndexes = player1Indexes;
                    defendingBoard = player2BoardCells;
                } else {
                    attackIndexes = player2Indexes;
                    defendingBoard = player1BoardCells;
                }
            }

        }
        player1.setScore(p1Score);
        player2.setScore(p2Score);
    }

    private static  <T> List<T> sublist(List<T> list, int ...indexes) {
        List<T> subList = new ArrayList<>();
        for (int index : indexes) {
            subList.add(list.get(index));
        }
        return subList;
    }

    public static Map<ShipType, List<Integer>> generateShipPlacement() {
        List<ShipType> shipTypes = Stream.of(ShipType.values()).filter(shipType -> shipType != ShipType.NONE).toList();

        List<BoardCell> board = new GamePlayer().getBoard();
        Map<String, List<BoardCell>> shipPlacementSlots = new HashMap<>();

        // Horizontal
        shipPlacementSlots.put("0-7", sublist(board, 0, 1, 2, 3, 4, 5, 6, 7));
        shipPlacementSlots.put("8-15", sublist(board, 8, 9, 10, 11, 12, 13, 14, 15));
        shipPlacementSlots.put("16-23", sublist(board, 16, 17, 18, 19, 20, 21, 22, 23));
        shipPlacementSlots.put("24-31", sublist(board, 24, 25, 26, 27, 28, 29, 30, 31));
        shipPlacementSlots.put("32-39", sublist(board, 32, 33, 34, 35, 36, 37, 38, 39));
        shipPlacementSlots.put("40-47", sublist(board, 40, 41, 42, 43, 44, 45, 46, 47));
        shipPlacementSlots.put("48-55", sublist(board, 48, 49, 50, 51, 52, 53, 54, 55));
        shipPlacementSlots.put("56-63", sublist(board, 56, 57, 58, 59, 60, 61, 62, 63));

        // Vertical
        shipPlacementSlots.put("0-56", sublist(board, 0, 8, 16, 24, 32, 40, 48, 56));
        shipPlacementSlots.put("1-57", sublist(board, 1, 9, 17, 25, 33, 41, 49, 57));
        shipPlacementSlots.put("2-58", sublist(board, 2, 10, 18, 26, 34, 42, 50, 58));
        shipPlacementSlots.put("3-59", sublist(board, 3, 11, 19, 27, 35, 43, 51, 59));
        shipPlacementSlots.put("4-60", sublist(board, 4, 12, 20, 28, 36, 44, 52, 60));
        shipPlacementSlots.put("5-61", sublist(board, 5, 13, 21, 29, 37, 45, 53, 61));
        shipPlacementSlots.put("6-62", sublist(board, 6, 14, 22, 30, 38, 46, 54, 62));
        shipPlacementSlots.put("7-63", sublist(board, 7, 15, 23, 31, 39, 47, 55, 63));

        Map<ShipType, List<Integer>> shipPlacementIndexes = new HashMap<>();

        shipTypes.forEach(shipType -> {
            while(true) {
                int randomSlotIndex = new Random().nextInt(0, shipPlacementSlots.size() - 1);
                String key = shipPlacementSlots.keySet().stream().toList().get(randomSlotIndex);
                List<BoardCell> slot = shipPlacementSlots.get(key);

                if (slot.stream().filter(boardCell -> boardCell.getShipType() == ShipType.NONE).count() < shipType.getSize()) {
                    // slot is too small for ship
                    continue;
                }

                // find a sequence of indexes that are not already occupied by a ship
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < slot.size(); i++) {
                    if (slot.get(i).getShipType() == ShipType.NONE) {
                        indexes.add(i);
                    } else {
                        indexes.clear();
                    }

                    if (indexes.size() == shipType.getSize()) {
                        break;
                    }
                }

                List<BoardCell> shipPlacement = sublist(slot, indexes.stream().mapToInt(i -> i).toArray());
                shipPlacement.forEach(boardCell -> boardCell.setShipType(shipType));
                shipPlacementIndexes.put(shipType, shipPlacement.stream().map(BoardCell::getBoardIndex).toList());
                break;
            }
        });

        return shipPlacementIndexes;
    }

}
