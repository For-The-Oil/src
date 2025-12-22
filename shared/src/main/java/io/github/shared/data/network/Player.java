package io.github.shared.data.network;

import com.esotericsoftware.kryonet.Connection;

import io.github.shared.data.enums_types.ResourcesType;
import io.github.shared.data.gameobject.Deck;
import java.util.HashMap;
import java.util.UUID;

/**
 * Représente un joueur avec plusieurs Decks, bâtiments et unités.
 */
public class Player extends ClientNetwork {
    private HashMap<ResourcesType, Integer> resources;

    public Player() {
        super();
        this.resources = new HashMap<>();
    }

    public Player(UUID uuid, String name, HashMap<String, Deck> fullDeck, Deck deck, String token, Connection connection) {
        super(uuid, name, fullDeck, null, token, connection);
        this.resources = new HashMap<>();
    }

    // Constructeur corrigé : copie la connection depuis ClientNetwork
    public Player(ClientNetwork cn) {
        super(
            cn.getUuid(),
            cn.getUsername(),
            cn.getDecks(),
            cn.getUnlockedCards(),
            cn.getToken(),
            cn.getConnection()
        );
        this.setCurrent(cn.getCurrentDeck());
        this.resources = new HashMap<>();
    }

    public HashMap<ResourcesType, Integer> getResources() { return resources; }
    public void setResources(HashMap<ResourcesType, Integer> resources) { this.resources = resources; }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player{");
        sb.append("username='").append(getUsername()).append('\'');
        sb.append(", uuid=").append(getUuid());
        sb.append(", decks=").append(getDecks().keySet());
        sb.append(", currentDeck=").append(getCurrentDeck() != null ? getCurrentDeck() : "null");
        sb.append(", unlockedCards=").append(getUnlockedCards());
        sb.append(", resources=").append(resources);
        sb.append('}');
        return sb.toString();
    }

}
