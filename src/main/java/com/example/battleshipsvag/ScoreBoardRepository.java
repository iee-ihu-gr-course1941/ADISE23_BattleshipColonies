package com.example.battleshipsvag;

import com.example.battleshipsvag.data.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScoreBoardRepository extends CrudRepository<Game, Long> {

    @Query("SELECT g FROM Game g ORDER BY g.id DESC")
    List<Game> findAllOrderByIDDesc();
}
