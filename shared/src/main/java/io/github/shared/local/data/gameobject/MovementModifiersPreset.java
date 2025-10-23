package io.github.shared.local.data.gameobject;

import java.util.EnumMap;
import io.github.shared.local.data.nameEntity.CellType;
import io.github.shared.local.data.nameEntity.UnitType;

public final class  MovementModifiersPreset {

    // CellType -> Unit Type -> CONSTANT
    public static EnumMap<CellType, EnumMap<UnitType, Float>> cellStats;

}
