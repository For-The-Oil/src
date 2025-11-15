package io.github.shared.data.requests;

import java.util.UUID;

/**
 * Représente un ordre donné par un joueur, avec des paramètres obligatoires selon le type.
 */
public class Request {
    private long timestamp;
    private UUID player;

    public Request() {
        this.timestamp= System.currentTimeMillis();
        this.player = null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long x){this.timestamp=x;}

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }
}
