package io.github.shared.local.shared_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.shared.local.data.EnumsTypes.RessourcesType;
import io.github.shared.local.data.network.Player;
import io.github.shared.local.data.snapshot.EntitySnapshot;

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


    public static void subtractResourcesInPlace(HashMap<RessourcesType, Integer> base, HashMap<RessourcesType, Integer> toSubtract) {
        for (Map.Entry<RessourcesType, Integer> entry : toSubtract.entrySet()) {
            RessourcesType type = entry.getKey();
            int valueToSubtract = entry.getValue();

            // Récupérer la valeur actuelle (0 si absente)
            int currentValue = base.getOrDefault(type, 0);

            // Soustraction
            int newValue = currentValue - valueToSubtract;

            // Mettre à jour (option : éviter les valeurs négatives)
            base.put(type, Math.max(newValue, 0));
        }
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
