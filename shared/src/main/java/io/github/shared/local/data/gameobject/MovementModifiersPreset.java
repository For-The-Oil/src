package io.github.shared.local.data.gameobject;

import java.util.EnumMap;
import io.github.shared.local.data.EnumsTypes.CellType;
import io.github.shared.local.data.EnumsTypes.EntityType;

public final class  MovementModifiersPreset {

    // CellType -> Unit Type -> CONSTANT
    public static EnumMap<CellType, EnumMap<EntityType, Float>> cellStats;

}
