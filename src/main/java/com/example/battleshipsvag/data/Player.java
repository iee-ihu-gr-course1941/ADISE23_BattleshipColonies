package com.example.battleshipsvag.data;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Player extends AbstractEntity {
    private String playerName;
    private String password = "NO PASSWORD";
}
