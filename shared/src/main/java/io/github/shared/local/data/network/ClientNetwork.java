package io.github.shared.local.data.network;
import com.esotericsoftware.kryonet.Connection;

import java.util.UUID;
import io.github.shared.local.data.gameobject.Deck;

public class ClientNetwork {
    private String token;
    private UUID uuid;
    private String username;
    private Deck deck;
    private transient Connection connection;

    public ClientNetwork(){}
    public ClientNetwork(UUID uuid, String name, Deck deck, String token, Connection connection) {
        this.token=token;
        this.uuid=uuid;
        this.username=name;
        this.deck = deck;
        this.connection=connection;
    }

    public ClientNetwork(UUID uuid, String name, Deck deck, String token) {
        this.token=token;
        this.uuid=uuid;
        this.username=name;
        this.deck = deck;
        this.connection=null;
    }


    public String toString() {
        return "ClientNetwork{" +
            "username='" + username + '\'' +
            ", uuid=" + uuid +
            ", token='" + token + '\'' +
            ", deck=" + deck +
            ", connection=" + (connection != null ? connection.getID() : "null") +
            '}';
    }

}
