package io.github.shared.local.ecs.component;

import com.badlogic.ashley.core.Component;

/**
 * x = La colonne de la map ou se trouve le coin supérieur gauche de notre batîment
 * y = La ligne de la map ou se trouve le coin supérieur gauche de notre batîment
 * rotation = le sens dans lequel le batiment est posé, {0,1,2,3}
 */
public class BuildingPositionComponent implements Component {
    public int x,y, rotation;
}
