package io.github.server.game_engine.gdx_ai;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;

import java.util.ArrayList;

import io.github.shared.data.EnumsTypes.CellType;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public class PathfindingDemo {
    public static void main(String[] args) {
//         Création d'une carte simple 5x5 avec des cellules traversables
        Cell[][] cells = new Cell[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                cells[y][x] = new Cell(CellType.GRASS); // Assure-toi que Cell est traversable par défaut
//                if(1 == x && 1 == y)cells[y][x] = new Cell(CellType.VOID);
            }
        }
        Shape shape = new Shape(cells);

        // Définition des points de départ et d'arrivée
        MapNode start = new MapNode(0, 0,new ArrayList<>());
        MapNode end = new MapNode(4, 3,new ArrayList<>());


        // Prépare le pathfinder et le chemin
        MapGraph graph = new MapGraph(shape,null, null,end, EntityType.test, 1);
        IndexedAStarPathFinder<MapNode> pathFinder = new IndexedAStarPathFinder<>(graph);
        GraphPath<MapNode> path = new DefaultGraphPath<>();

        // Recherche du chemin
        pathFinder.searchNodePath(start, end, new MapHeuristic(), path);

        // Affiche le résultat
        System.out.println("Chemin trouvé :");
        if (path.getCount() == 0) {
            System.out.println("Aucun chemin trouvé !");
        } else {
            for (MapNode node : path) {
                System.out.println(" -> (" + node.x + ", " + node.y + ")");
            }
        }
    }
}

