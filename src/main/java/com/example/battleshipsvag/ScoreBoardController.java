package com.example.battleshipsvag;

import com.example.battleshipsvag.resources.ScoredGameResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scoreboard")
@RequiredArgsConstructor
public class ScoreBoardController {
    private final ScoreBoardService scoreBoardService;

    @GetMapping
    public ResponseEntity<List<ScoredGameResource>> getScoreBoard() {
        return ResponseEntity.ok(scoreBoardService.getScoreBoard());
    }

}

