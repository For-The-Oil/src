package io.github.shared.local.data.network;

import com.esotericsoftware.kryonet.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.github.shared.local.data.gameobject.Deck;

public class ClientNetwork {
    private transient String token;
    private transient UUID uuid;
    private String username;
    private long lastActivityTimestamp;
    private HashMap<String, Deck> decks;
    private Deck current;
    private transient String ip;
    private transient Connection connection;

    public ClientNetwork() {
        this.decks = new HashMap<>();
    }

    public ClientNetwork(UUID uuid, String username, HashMap<String, Deck> decks, String token, Connection connection) {
        this.token = token;
        this.uuid = uuid;
        this.username = username;
        this.decks = decks != null ? decks : new HashMap<>();
        this.connection = connection;
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    public ClientNetwork(UUID uuid, String name, HashMap<String, Deck> decks, String token) {
        this(uuid, name, decks, token, null);
    }

    public String toString() {
        return "ClientNetwork{" +
            "username='" + username + '\'' +
            ", uuid=" + uuid +
            ", token='" + token + '\'' +
            ", decks=" + decks.keySet() + // Affiche juste les noms des decks
            ", connection=" + (connection != null ? connection.getID() : "null") +
            '}';
    }

    // === Getters & Setters ===
    public HashMap<String, Deck> getDecks() {
        return decks;
    }

    public void addDeck(String name, Deck deck) {
        decks.put(name, deck);
    }

    public Deck getDeck(String name) {
        return decks.get(name);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public Connection getConnection() {
        return connection;
    }

    public long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public void actualizeLastActivityTimestamp(){
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    public void setLastActivityTimestamp(long lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Deck getCurrentDeck() {
        return current;
    }

    public void setCurrent(Deck current) {
        this.current = current;
    }


}
