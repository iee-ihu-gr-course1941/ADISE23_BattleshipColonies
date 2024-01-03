package com.example.battleshipsvag.resources;

import com.example.battleshipsvag.data.ShipType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter @Getter
public class ShipPlacementResource {
    private Map<ShipType, List<Integer>> shipPlacements = new HashMap<>();
}
