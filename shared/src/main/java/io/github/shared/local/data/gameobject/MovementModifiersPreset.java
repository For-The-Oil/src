package io.github.shared.local.data.gameobject;

import java.util.EnumMap;
import io.github.shared.local.data.EnumsTypes.CellType;
import io.github.shared.local.data.EnumsTypes.UnitType;

public final class  MovementModifiersPreset {

    // CellType -> Unit Type -> CONSTANT
    public static EnumMap<CellType, EnumMap<UnitType, Float>> cellStats;

}
