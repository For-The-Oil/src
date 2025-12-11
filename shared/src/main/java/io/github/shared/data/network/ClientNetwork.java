package io.github.shared.data.network;

import com.esotericsoftware.kryonet.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import io.github.shared.data.enums_types.EntityType;
import io.github.shared.data.gameobject.Deck;

public class ClientNetwork {
    private transient String token;
    private UUID uuid;
    private String username;
    private long lastActivityTimestamp;
    private HashMap<String, Deck> decks;
    private ArrayList<EntityType> unlockedCards;
    private Deck current;
    private transient String ip;
    private transient Connection connection;

    public ClientNetwork() {
        this.decks = new HashMap<>();
        this.unlockedCards = new ArrayList<>();
    }

    public ClientNetwork(UUID uuid, String username, HashMap<String, Deck> decks, ArrayList<EntityType> unlockedCards, String token, Connection connection) {
        this.token = token;
        this.uuid = uuid;
        this.username = username;
        this.decks = decks != null ? decks : new HashMap<>();
        this.connection = connection;
        this.lastActivityTimestamp = System.currentTimeMillis();
        this.unlockedCards = unlockedCards != null ? unlockedCards : new ArrayList<>();
    }

    public ClientNetwork(UUID uuid, String name, HashMap<String, Deck> decks, ArrayList<EntityType> unlockedCards, String token) {
        this(uuid, name, decks, unlockedCards, token, null);
    }

    // === Getters & Setters ===
    public HashMap<String, Deck> getDecks() { return decks; }
    public void addDeck(String name, Deck deck) { decks.put(name, deck); }
    public Deck getDeck(String name) { return decks.get(name); }
    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getToken() { return token; }
    public Connection getConnection() { return connection; }
    public long getLastActivityTimestamp() { return lastActivityTimestamp; }
    public void actualizeLastActivityTimestamp() { this.lastActivityTimestamp = System.currentTimeMillis(); }
    public void setLastActivityTimestamp(long lastActivityTimestamp) { this.lastActivityTimestamp = lastActivityTimestamp; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Deck getCurrentDeck() { return current; }
    public void setCurrent(Deck current) { this.current = current; }
    public ArrayList<EntityType> getUnlockedCards() { return unlockedCards; }
    public void setUnlockedCards(ArrayList<EntityType> unlockedCards) { this.unlockedCards = unlockedCards; }

    @Override
    public String toString() {
        return "ClientNetwork{" +
            "username='" + username + '\'' +
            ", uuid=" + uuid +
            ", token='" + token + '\'' +
            ", decks=" + decks.keySet() +
            ", connection=" + (connection != null ? connection.getID() : "null") +
            '}';
    }
}
