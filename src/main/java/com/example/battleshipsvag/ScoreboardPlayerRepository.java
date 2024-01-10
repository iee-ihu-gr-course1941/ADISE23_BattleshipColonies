package com.example.battleshipsvag;

import com.example.battleshipsvag.data.Game;
import com.example.battleshipsvag.data.Player;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScoreboardPlayerRepository extends CrudRepository<Player, Long> {

    @Query("SELECT g FROM Player g ORDER BY g.wins DESC")
    List<Player> findAllOrderByWinsDesc();
}
