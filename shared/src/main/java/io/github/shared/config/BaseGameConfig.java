package io.github.shared.config;

import static io.github.shared.data.enums_types.GameModeType.ALPHA_TEST;
import static io.github.shared.data.enums_types.GameModeType.CLASSIC;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.shared.data.enums_types.GameModeType;

public final class BaseGameConfig {

    public static int MAX_TIME;
    public static float GAME_SPEED;
    public static float RESSOURCE_COEF;
    public static float SPEED_COEF;
    public static float DAMAGE_COEF;
    public static float BUILDING_COST_COEF;
    public static final float ARMOR_COEF = 0.5f;
    public static final float FIXED_TIME_STEP = 16.666f;
    public static final float DESTROY_PATH_COST = 1.5f;
    public static final float CELL_SIZE = 100f;

    public static final ArrayList<GameModeType> ALLOWED_GAME_MOD = new ArrayList<>(Arrays.asList(
        CLASSIC, ALPHA_TEST
    ));





}
