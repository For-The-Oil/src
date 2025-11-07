package io.github.server.config;

import static io.github.shared.local.data.EnumsTypes.GameModeType.ALPHA_TEST;
import static io.github.shared.local.data.EnumsTypes.GameModeType.CLASSIC;
import static io.github.shared.local.data.EnumsTypes.MapName.*;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.shared.local.data.EnumsTypes.GameModeType;
import io.github.shared.local.data.EnumsTypes.MapName;

public final class BaseGameConfig {

    public static int MAX_TIME;
    public static float GAME_SPEED;
    public static float RESSOURCE_COEF;
    public static float SPEED_COEF;
    public static float DAMAGE_COEF;
    public static float ARMOR_COEF;
    public static float BUILDING_COST_COEF;
    public static final float FIXED_TIME_STEP = 16.666f;

    public static final ArrayList<GameModeType> ALLOWED_GAME_MOD = new ArrayList<>(Arrays.asList(
        CLASSIC, ALPHA_TEST
    ));





}
