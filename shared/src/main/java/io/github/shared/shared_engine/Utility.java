package io.github.shared.shared_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.shared.data.EnumsTypes.ResourcesType;
import io.github.shared.data.network.Player;
import io.github.shared.data.snapshot.EntitySnapshot;

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


    public static ArrayList<Integer> extractNetIds(ArrayList<EntitySnapshot> snapshots) {
        ArrayList<Integer> netIds = new ArrayList<>();
        if (snapshots == null) return netIds;

        for (EntitySnapshot snapshot : snapshots) {
            if (snapshot != null) {
                netIds.add(snapshot.getNetId());
            }
        }
        return netIds;
    }


}
