package io.github.server.network;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;

/**
 * Gère la connexion PostgreSQL et les opérations sur les utilisateurs :
 * inscription, login, et connexion par token.
 */
public class DatabaseManager {

    private static DatabaseManager INSTANCE;
    private final Jdbi jdbi;

    private DatabaseManager() {
        // === CONFIG HIKARI ===
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/fortheoildb");
        config.setUsername("postgres");
        config.setPassword("password");
        config.setMaximumPoolSize(5);
        config.setDriverClassName("org.postgresql.Driver");

        HikariDataSource dataSource = new HikariDataSource(config);

        // === INIT JDBI ===
        this.jdbi = Jdbi.create(dataSource);

        // Crée la table si elle n'existe pas
        jdbi.useHandle(handle -> handle.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
                "id UUID PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "token TEXT" +
                ")"
        ));

    }

    public static DatabaseManager getInstance() {
        if (INSTANCE == null) INSTANCE = new DatabaseManager();
        return INSTANCE;
    }

    // === INSCRIPTION ===
    public boolean registerUser(String username, String password) {
        // Vérifie si le nom existe déjà
        if (userExists(username)) return false;

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        UUID id = UUID.randomUUID();

        jdbi.useHandle(handle ->
            handle.execute("INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)",
                id, username, hash)
        );
        return true;
    }

    // === LOGIN PAR IDENTIFIANTS ===
    public Optional<String> login(String username, String password) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT password_hash FROM users WHERE username = :u")
                .bind("u", username)
                .mapTo(String.class)
                .findOne()
                .filter(hash -> BCrypt.checkpw(password, hash))
                .map(ok -> {
                    String token = UUID.randomUUID().toString();
                    handle.execute("UPDATE users SET token = ? WHERE username = ?", token, username);
                    return token;
                })
        );
    }

    // === LOGIN PAR TOKEN ===
    public boolean loginByToken(String token) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM users WHERE token = :t")
                .bind("t", token)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }

    // === VÉRIFIE SI L'USER EXISTE ===
    public boolean userExists(String username) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM users WHERE username = :u")
                .bind("u", username)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }
}
