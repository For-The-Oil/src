package io.github.shared.local.data.component;

import com.artemis.Component;

/**
 * x = La colonne de la map ou se trouve le coin supérieur gauche de notre batîment
 * y = La ligne de la map ou se trouve le coin supérieur gauche de notre batîment
 * rotation = le sens dans lequel le batiment est posé, {0,1,2,3}
 */

import com.artemis.annotations.PooledWeaver;

import io.github.shared.local.data.EnumsTypes.Direction;

@PooledWeaver
public class BuildingMapPositionComponent extends Component {
    public int x;
    public int y;
    public Direction direction;
}
