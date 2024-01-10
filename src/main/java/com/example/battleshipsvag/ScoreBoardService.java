package com.example.battleshipsvag;

import com.example.battleshipsvag.data.GamePlayer;
import com.example.battleshipsvag.resources.ScoredGameResource;
import com.example.battleshipsvag.resources.ScoredPlayerResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreBoardService {

    private final ScoreBoardRepository scoreBoardRepository;
    private final ScoreboardPlayerRepository scoreboardPlayerRepository;
    public List<ScoredGameResource> getScoreBoard() {
        return scoreBoardRepository.findAllOrderByIDDesc().stream()
                .parallel()
                .map(game -> {
                    ScoredGameResource scoredGameResource = new ScoredGameResource();
                    scoredGameResource.setGameId(game.getId().toString());
                    GamePlayer player1 = game.getPlayer1();
                    scoredGameResource.setPlayer1Name(player1.getPlayerName());
                    scoredGameResource.setPlayer1Score(player1.getScore());

                    GamePlayer player2 = game.getPlayer2();
                    scoredGameResource.setPlayer2Name(player2.getPlayerName());
                    scoredGameResource.setPlayer2Score(player2.getScore());
                    scoredGameResource.setWinner(game.getWinner());
                    return scoredGameResource;
                })
                .toList();

    }

    public List<ScoredPlayerResource> getPlayers() {
        return scoreboardPlayerRepository.findAllOrderByWinsDesc().stream()
                .parallel()
                .map(player -> {
                    ScoredPlayerResource scoredPlayerResource = new ScoredPlayerResource();
                    scoredPlayerResource.setPlayerName(player.getPlayerName());
                    scoredPlayerResource.setPlayerScore(player.getWins());
                    return scoredPlayerResource;
                })
                .toList();

    }

}
