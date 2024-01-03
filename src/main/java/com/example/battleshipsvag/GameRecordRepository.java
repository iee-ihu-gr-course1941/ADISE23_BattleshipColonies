package com.example.battleshipsvag;

import com.example.battleshipsvag.data.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRecordRepository extends CrudRepository<Game, Long> {

        @Override
        <S extends Game> S save(S entity);

        @Query("SELECT g FROM Game g WHERE g.id = (SELECT MAX(g2.id) FROM Game g2) and g.status != com.example.battleshipsvag.data.GameStatus.FINISHED")
        Optional<Game> findCurrentNotFinishedGame();


        @Override
        List<Game> findAll();
}
