package com.example.battleshipsvag;

import com.example.battleshipsvag.gen.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BattleShipsVagApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(BattleShipsVagApplication.class, args);
    }


    private final DataGenerator dataGenerator;
    @Override
    public void run(String... args) {
        dataGenerator.generate();
    }
}
