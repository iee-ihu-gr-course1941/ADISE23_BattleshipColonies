package com.example.battleshipsvag;

import com.example.battleshipsvag.data.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {

    @Override
    <S extends Player> S save(S entity);

    Optional<Player> findByPlayerName(String playerName);
}
