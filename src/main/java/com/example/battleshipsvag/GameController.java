package com.example.battleshipsvag;

import com.example.battleshipsvag.auth.JwtUtil;
import com.example.battleshipsvag.resources.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    private static final String PLAYER_TOKEN_HEADER = "X-Player-Token";
    private final GameService gameService;
    @PostMapping("/join")
    public ResponseEntity<GameResource> joinGame(@RequestBody JoinGameResource playerName) {
        return ResponseEntity
                .ok()
                .header(PLAYER_TOKEN_HEADER, JwtUtil.generateToken(playerName.getPlayerName()))
                .body(gameService.joinGame(playerName.getPlayerName()));
    }

    @GetMapping
    public ResponseEntity<GameResource> getPlayerProfile(@RequestHeader(PLAYER_TOKEN_HEADER) String token) {
       return ResponseEntity.ok(gameService.getGameResourceForPlayer(JwtUtil.getPlayerNameFromToken(token)));
    }

    @PutMapping("/start")
    public ResponseEntity<GameResource> startGame(@RequestHeader(PLAYER_TOKEN_HEADER) String token) {
        return ResponseEntity.ok(gameService.startGame(JwtUtil.getPlayerNameFromToken(token)));
    }

    @PutMapping("/place")
    public ResponseEntity<GameResource> placeShip(@RequestHeader(PLAYER_TOKEN_HEADER) String token, @RequestBody ShipPlacementResource shipPlacementResource) {
        return ResponseEntity.ok(gameService.placeShip(JwtUtil.getPlayerNameFromToken(token), shipPlacementResource.getShipPlacements()));
    }

    @PutMapping("/attack")
    public ResponseEntity<AttackResultResource> attack(@RequestHeader(PLAYER_TOKEN_HEADER) String token, @RequestBody AttackResource attackResource) {;
        return ResponseEntity.ok(gameService.attack(JwtUtil.getPlayerNameFromToken(token), attackResource.getAttackPosition()));
    }


}
