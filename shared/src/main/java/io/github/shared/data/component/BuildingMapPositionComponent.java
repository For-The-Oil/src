package io.github.shared.data.component;

/**
 * x = La colonne de la map ou se trouve le coin supérieur gauche de notre batîment
 * y = La ligne de la map ou se trouve le coin supérieur gauche de notre batîment
 * rotation = le sens dans lequel le batiment est posé, {0,1,2,3}
 */

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import io.github.shared.data.enumsTypes.Direction;


/**
 * x = La colonne de la map où se trouve le coin supérieur gauche du bâtiment
 * y = La ligne de la map où se trouve le coin supérieur gauche du bâtiment
 * direction = le sens dans lequel le bâtiment est posé, {0,1,2,3}
 */
@PooledWeaver
public class BuildingMapPositionComponent extends PooledComponent {
    public int x;
    public int y;
    public Direction direction;

    @Override
    public void reset() {
        x = -1;
        y = -1;
        direction = null;
    }

    public void set(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
}

