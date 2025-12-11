package io.github.shared.shared_engine;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.shared.config.BaseGameConfig;
import io.github.shared.data.component.ProprietyComponent;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.enums_types.Direction;
import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.component.NetComponent;
import io.github.shared.data.component.PositionComponent;
import io.github.shared.data.enums_types.ShapeType;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;
import io.github.shared.data.network.Player;
import io.github.shared.data.snapshot.EntitySnapshot;
import io.github.shared.shared_engine.manager.ShapeManager;

public class Utility {
    private static final AtomicInteger COUNTER = new AtomicInteger((int) System.currentTimeMillis());

    public static int getNetId() {
        int base = COUNTER.getAndIncrement();
        int rnd  = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        int id   = base ^ rnd;
        if (id == 0) id = 1;
        return id & 0x7fffffff;
    }

    public static Player findPlayerByUuid(ArrayList<Player> players, UUID uuid) {
        if (players == null || uuid == null) {
            return null;
        }

        for (Player player : players) {
            if (uuid.equals(player.getUuid())) {
                return player;
            }
        }

        return null; // Aucun client trouvé
    }


    public static String findTeamByPlayer(Player player, HashMap<String, ArrayList<Player>> playerTeam) {
        for (Map.Entry<String, ArrayList<Player>> entry : playerTeam.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        System.err.print("Aucune équipe trouvée"+player.toString());
        return null;
    }


    public static void addResourcesInPlace(HashMap<ResourcesType, Integer> base, HashMap<ResourcesType, Integer> toAdd) {
        if (base == null || toAdd == null) return;

        for (Map.Entry<ResourcesType, Integer> e : toAdd.entrySet()) {
            ResourcesType type = e.getKey();
            int delta = (e.getValue() == null) ? 0 : e.getValue();
            int current = base.getOrDefault(type, 0);
            base.put(type, current + delta);
        }
    }


    public static void subtractResourcesInPlace(HashMap<ResourcesType, Integer> base, HashMap<ResourcesType, Integer> toSubtract) {
        for (Map.Entry<ResourcesType, Integer> entry : toSubtract.entrySet()) {
            ResourcesType type = entry.getKey();
            int valueToSubtract = entry.getValue();

            // Récupérer la valeur actuelle (0 si absente)
            int currentValue = base.getOrDefault(type, 0);

            // Soustraction
            int newValue = currentValue - valueToSubtract;

            // Mettre à jour (option : éviter les valeurs négatives)
            base.put(type, Math.max(newValue, 0));
        }
    }


    public static boolean canSubtractResources(HashMap<ResourcesType, Integer> base, HashMap<ResourcesType, Integer> toSubtract) {
        if (base == null || toSubtract == null) return false;

        for (Map.Entry<ResourcesType, Integer> e : toSubtract.entrySet()) {
            ResourcesType type = e.getKey();
            int valueToSubtract = (e.getValue() == null) ? 0 : e.getValue();

            if (valueToSubtract <= 0) continue; // rien à retirer pour cette ressource

            int current = base.getOrDefault(type, 0);
            if (current - valueToSubtract < 0) {
                return false; // au moins une ressource deviendrait négative
            }
        }
        return true; // toutes les soustractions restent >= 0
    }

    /** Convert world coordinate (float) to cell index (int). */
    public static int worldToCell(float worldCoordinate) {
        return (int) Math.floor(worldCoordinate / BaseGameConfig.CELL_SIZE);
    }

    /** Convert cell index (int) to world coordinate (float). */
    public static float cellToWorld(int cellIndex) {
        return cellIndex * BaseGameConfig.CELL_SIZE;
    }

    public static boolean isRangedDistanceValid(PositionComponent attackerPos, PositionComponent targetPos, float range, Shape map) {
        return isRangedDistanceValid(attackerPos,targetPos.x,targetPos.y,targetPos.z,range,map);
    }
    public static boolean isRangedDistanceValid(PositionComponent attackerPos,float targetPosX,float targetPosY,float targetPosZ, float range, Shape map) {
        // Early-out: quick spherical range check in continuous 3D space.
        // If the target is farther than 'range', there's no need to perform an expensive line-of-sight (LoS) test.
        final float dx = targetPosX - attackerPos.x;
        final float dy = targetPosY - attackerPos.y;
        final float dz = targetPosZ - attackerPos.z;
        final float dist2 = dx*dx + dy*dy + dz*dz;
        if (dist2 > (range * range)) return false; // too far: reject immediately

        // Convert world-space coordinates to discrete tile indices.
        // IMPORTANT: replace worldToCellX/Y(...) with your own formula (tile size, origin, scaling, etc.).
        // This step maps continuous positions into the grid used by Shape/Cell.
        final int sx = Utility.worldToCell(attackerPos.x); // start cell X (attacker)
        final int sy = Utility.worldToCell(attackerPos.y); // start cell Y (attacker)
        final int tx = Utility.worldToCell(targetPosX);   // target cell X
        final int ty = Utility.worldToCell(targetPosY);   // target cell Y

        // Fetch the map (Shape) to query bounds and cells.
        // If either endpoint is outside the map, consider LoS blocked to avoid undefined accesses.
        if (!map.isValidPosition(sx, sy) || !map.isValidPosition(tx, ty)) {
            return false; // out of bounds: treat as obstructed
        }

        // Bresenham’s line traversal: iterates all grid cells intersected by the segment (sx,sy) → (tx,ty).
        // We check every visited cell for traversability; any non-traversable cell blocks the shot.
        int x = sx, y = sy;
        int dxg = Math.abs(tx - sx);
        int dyg = Math.abs(ty - sy);
        int stepX = (sx < tx) ? 1 : -1;
        int stepY = (sy < ty) ? 1 : -1;
        int err = dxg - dyg;

        // Bresenham's line: iterate cells from (sx, sy) to (tx, ty) without while(true)
        // We process the current cell, then stop once we have reached the target cell.
        do {
            // Bounds check for safety at each step; reject if we step outside the map.
            if (!map.isValidPosition(x, y)) return false;

            // Read the current grid cell along the path and verify it is traversable.
            // Rule: cell.getCellType().isTraversable(null) must be true for LoS to remain clear.
            Cell cell = map.getCells(x, y);
            if (cell != null && !cell.getCellType().isTraversable(null)) {
                return false; // obstacle encountered: LoS blocked
            }

            // Bresenham step: advance along the dominant axis while correcting the error for the minor axis.
            int e2 = 2 * err;
            if (e2 > -dyg) { err -= dyg; x += stepX; }
            if (e2 <  dxg) { err += dxg; y += stepY; }

            // Loop ends when we have just processed the target cell
        } while (x != tx || y != ty);


        // All cells along the segment are traversable: the ranged shot is valid.
        return true;
    }
    public static ArrayList<Float> isRangedDistanceValidForBuilding(PositionComponent attackerPos, PositionComponent targetPos, float range,Shape map , ShapeType shapeType, Direction direction) {
        Shape s = ShapeManager.rotateShape(shapeType.getShape(), direction);
        ArrayList<Float> arrayList = new ArrayList<>();
        float bestDist2 = Float.MAX_VALUE;
        float range2 = range * range;
        for(int i = 0; i < s.getWidth() ; i++){
            for(int j = 0; j < s.getHeight() ; j++){
                if(s.isValidPosition(i,j)&& !s.getCells(i,j).getCellType().equals(CellType.VOID)){
                    float x = targetPos.x + cellToWorld(i);
                    float y = targetPos.y + cellToWorld(j);
                    float z = targetPos.z;
                    if(isRangedDistanceValid(attackerPos,x,y,z,range,map)) {
                        float dx = x - attackerPos.x;
                        float dy = y - attackerPos.y;
                        float dz = z - attackerPos.z;
                        float dist2 = dx * dx + dy * dy + dz * dz;
                        if (dist2 <= range2 && dist2 < bestDist2) {
                            bestDist2 = dist2;
                            ArrayList<Float> tmp = new ArrayList<>();
                            tmp.add(x);
                            tmp.add(y);
                            arrayList = new ArrayList<>();
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    public static ArrayList<Float> isAttackValidForBuilding(PositionComponent attackerPos, PositionComponent targetPos, float reach, ShapeType shapeType, Direction direction) {
        Shape s = ShapeManager.rotateShape(shapeType.getShape(), direction);
        ArrayList<Float> arrayList = new ArrayList<>();
        float bestDist2 = Float.MAX_VALUE;
        float reach2 = reach * reach;
        for(int i = 0; i < s.getWidth() ; i++){
            for(int j = 0; j < s.getHeight() ; j++){
                if(s.isValidPosition(i,j)&& !s.getCells(i,j).getCellType().equals(CellType.VOID)){
                    float x = targetPos.x + cellToWorld(i);
                    float y = targetPos.y + cellToWorld(j);
                    float z = targetPos.z;
                    float dx = x - attackerPos.x;
                    float dy = y - attackerPos.y;
                    float dz = z - attackerPos.z;
                    float dist2 = dx*dx + dy*dy + dz*dz;
                    if (dist2 <= reach2 && dist2 < bestDist2){
                        bestDist2 = dist2;
                        ArrayList<Float> tmp = new ArrayList<>();
                        tmp.add(x);
                        tmp.add(y);
                        arrayList = new ArrayList<>();
                    }
                }
            }
        }
        return arrayList;
    }


}
