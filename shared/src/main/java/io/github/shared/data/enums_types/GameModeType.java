package io.github.shared.data.enums_types;

import static io.github.shared.data.enums_types.MapName.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public enum GameModeType {
    CLASSIC(new ArrayList<>(Arrays.asList(BASIC, BEACH, VOLCAN, GLACIER, TRAINING_GROUND, WASTELAND)), 2,2, 3600),
    ALPHA_TEST(new ArrayList<>(Collections.singletonList(BETA_TEST)), 1,1, 999999);

    private final ArrayList<MapName> associated_map;
    private final int MAX_PLAYER;
    private final int MIN_PLAYER;
    private final int MAX_GAME_TIME; //seconds
    GameModeType(ArrayList<MapName> associatedMap, int maxPlayer, int minPlayer, int maxGameTime){
        associated_map = associatedMap;
        MAX_PLAYER = maxPlayer;
        MIN_PLAYER = minPlayer;
        MAX_GAME_TIME = maxGameTime;
    }

    public int getMAX_GAME_TIME() {
        return MAX_GAME_TIME;
    }

    public int getMIN_PLAYER() {
        return MIN_PLAYER;
    }
    public int getMAX_PLAYER() {
        return MAX_PLAYER;
    }

    public ArrayList<MapName> getAssociated_map() {
        return associated_map;
    }

}
