package io.github.server.server_engine.manager;

import static io.github.server.server_engine.utils.JsonUtils.parseDecksJson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.mindrot.jbcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import io.github.server.config.DatabaseConfig;
import io.github.server.data.network.UserData;
import io.github.server.server_engine.utils.JsonUtils;
import io.github.shared.local.data.EnumsTypes.DeckCardCategory;
import io.github.shared.local.data.gameobject.Deck;


/**
 * The {@code DatabaseManager} class serves as the central data access layer
 * for all user and deck operations within the application.
 * <p>
 * It uses:
 * <ul>
 *   <li>{@link com.zaxxer.hikari.HikariCP} for efficient database connection pooling,</li>
 *   <li>{@link org.jdbi.v3.core.Jdbi} for simplified SQL operations,</li>
 *   <li>{@link com.fasterxml.jackson.databind.ObjectMapper} for JSON serialization/deserialization,</li>
 *   <li>{@link org.mindrot.jbcrypt.BCrypt} for secure password hashing.</li>
 * </ul>
 * <p>
 * The class implements a <b>singleton pattern</b> to ensure that all database operations
 * use the same connection pool instance.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Initialize and manage the PostgreSQL connection pool</li>
 *   <li>Register and authenticate users</li>
 *   <li>Manage deck data (CRUD operations via JSONB)</li>
 *   <li>Retrieve structured {@link io.github.core.user.UserData} objects for authenticated users</li>
 * </ul>
 *
 * <h2>Database Tables</h2>
 * <ul>
 *   <li><b>user</b> — stores UUID, email, username, and password hash</li>
 *   <li><b>deck</b> — stores user_id, deck name, and deck data (as JSONB)</li>
 * </ul>
 *
 * <p>
 * All JSON serialization is handled using Jackson.
 * </p>
 *
 * @author
 * @version 1.0
 * @since 2025-10
 */
public class DatabaseManager {

    /** JSON serializer/deserializer for deck data. */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Singleton instance of DatabaseManager. */
    private static DatabaseManager INSTANCE;

    /** JDBI handle for SQL operations. */
    private final Jdbi jdbi;

    /**
     * Private constructor initializing the database connection pool and JDBI instance.
     * <p>
     * Uses {@link com.zaxxer.hikari.HikariConfig} and {@link com.zaxxer.hikari.HikariDataSource}
     * to configure the PostgreSQL database connection.
     * </p>
     */
    private DatabaseManager() {
        // === HikariCP Configuration ===
        HikariConfig config = new HikariConfig();
        String jdbcUrl = String.format(
            "jdbc:postgresql://%s:%s/%s",
            DatabaseConfig.hostname,
            DatabaseConfig.PORT,
            DatabaseConfig.uri
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(DatabaseConfig.user);
        config.setPassword(DatabaseConfig.password);
        config.setMaximumPoolSize(5); // Maximum number of simultaneous connections
        config.setDriverClassName("org.postgresql.Driver");

        HikariDataSource dataSource = new HikariDataSource(config);

        // === Initialize JDBI ===
        this.jdbi = Jdbi.create(dataSource);
    }

    /**
     * Returns the singleton instance of {@link DatabaseManager}.
     *
     * @return the global DatabaseManager instance
     */
    public static DatabaseManager getInstance() {
        if (INSTANCE == null) INSTANCE = new DatabaseManager();
        return INSTANCE;
    }

    // -----------------------
    // User methods (existing)
    // -----------------------

    /**
     * Registers a new user in the database.
     * <p>
     * Hashes the password with BCrypt and inserts a new record into the {@code user} table.
     * </p>
     *
     * @param email    the email of the new user (must be unique)
     * @param username the username of the new user
     * @param password the plain text password of the new user
     * @return {@code true} if the user was successfully registered, {@code false} if the email already exists
     */
    public boolean registerUser(String email, String username, String password) {
        if (userExists(email)) return false;

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        UUID id = UUID.randomUUID();

        jdbi.useHandle(handle ->
            handle.execute(
                "INSERT INTO \"user\" (id, email, username, password_hash) VALUES (?, ?, ?, ?)",
                id, email, username, hash
            )
        );
        return true;
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    the user's email
     * @param password the user's plain text password
     * @return {@code true} if credentials match, {@code false} otherwise
     */
    public boolean login(String email, String password) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT password_hash FROM \"user\" WHERE email = :e")
                .bind("e", email)
                .mapTo(String.class)
                .findOne()
                .map(hash -> BCrypt.checkpw(password, hash))
                .orElse(false)
        );
    }

    /**
     * Authenticates a user by UUID and password.
     *
     * @param id       the UUID of the user
     * @param password the user's plain text password
     * @return {@code true} if credentials match, {@code false} otherwise
     */
    public boolean login(UUID id, String password) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT password_hash FROM \"user\" WHERE id = :i")
                .bind("i", id)
                .mapTo(String.class)
                .findOne()
                .map(hash -> BCrypt.checkpw(password, hash))
                .orElse(false)
        );
    }

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email to check
     * @return {@code true} if the user exists, {@code false} otherwise
     */
    public boolean userExists(String email) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM \"user\" WHERE email = :e")
                .bind("e", email)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }

    /**
     * Checks whether a user with the given UUID exists.
     *
     * @param id the UUID to check
     * @return {@code true} if the user exists, {@code false} otherwise
     */
    public boolean userExists(UUID id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM \"user\" WHERE id = :i")
                .bind("i", id)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }

    // -----------------------
    // Deck methods (new)
    // -----------------------

    /**
     * Retrieves the UUID of a user by email.
     *
     * @param email the user's email
     * @return an {@link Optional} containing the UUID, or empty if not found
     */
    private Optional<UUID> getUserIdByEmail(String email) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT id FROM \"user\" WHERE email = :e")
                .bind("e", email)
                .mapTo(UUID.class)
                .findOne()
        );
    }

    /**
     * Retrieves all decks (as JSON) associated with the user identified by email.
     *
     * @param email the user's email
     * @return a list of JSON strings (may be empty)
     */
    public List<String> getDecksJsonByEmail(String email) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return Collections.emptyList();
        return getDecksJsonByUserId(maybeId.get());
    }

    /**
     * Retrieves all decks (as JSON) associated with the user identified by UUID.
     *
     * @param userId the user's UUID
     * @return a list of JSON strings (may be empty)
     */
    public List<String> getDecksJsonByUserId(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT data::text FROM deck WHERE user_id = :uid")
                .bind("uid", userId)
                .mapTo(String.class)
                .list()
        );
    }

    /**
     * Retrieves a specific deck by email and deck name.
     *
     * @param email    the user's email
     * @param deckName the name of the deck
     * @return an {@link Optional} containing the deck JSON, or empty if not found
     */
    public Optional<String> getDeckJsonByEmailAndName(String email, String deckName) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return Optional.empty();
        return getDeckJsonByUserIdAndName(maybeId.get(), deckName);
    }

    /**
     * Retrieves a specific deck by user ID and deck name.
     *
     * @param userId   the user's UUID
     * @param deckName the name of the deck
     * @return an {@link Optional} containing the deck JSON, or empty if not found
     */
    public Optional<String> getDeckJsonByUserIdAndName(UUID userId, String deckName) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT data::text FROM deck WHERE user_id = :uid AND name = :name")
                .bind("uid", userId)
                .bind("name", deckName)
                .mapTo(String.class)
                .findOne()
        );
    }

    /**
     * Inserts or updates a deck (upsert) for the specified user by email.
     *
     * @param email    the user's email
     * @param deckName the name of the deck
     * @param jsonData the deck data in JSON format
     * @return {@code true} if the operation succeeded, {@code false} if the user was not found
     */
    public boolean setDeckJsonByEmail(String email, String deckName, String jsonData) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return false;
        return setDeckJsonByUserId(maybeId.get(), deckName, jsonData);
    }

    /**
     * Inserts or updates a deck (upsert) for the specified user by UUID.
     *
     * @param userId   the user's UUID
     * @param deckName the name of the deck
     * @param jsonData the deck data in JSON format
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    public boolean setDeckJsonByUserId(UUID userId, String deckName, String jsonData) {
        return jdbi.withHandle(handle -> {
            // Try update first
            int updated = handle.createUpdate("UPDATE deck SET data = CAST(:data AS JSONB) WHERE user_id = :uid AND name = :name")
                .bind("data", jsonData)
                .bind("uid", userId)
                .bind("name", deckName)
                .execute();

            if (updated > 0) {
                return true;
            }

            // If no row updated, insert new deck
            int inserted = handle.createUpdate("INSERT INTO deck (user_id, name, data) VALUES (:uid, :name, CAST(:data AS JSONB))")
                .bind("uid", userId)
                .bind("name", deckName)
                .bind("data", jsonData)
                .execute();

            return inserted > 0;
        });
    }

    /**
     * Returns the number of decks owned by a user identified by email.
     *
     * @param email the user's email
     * @return the number of decks, or 0 if the user was not found
     */
    public int getDeckCountByEmail(String email) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return 0;
        return getDeckCountByUserId(maybeId.get());
    }

    /**
     * Returns the number of decks owned by a user identified by UUID.
     *
     * @param userId the user's UUID
     * @return the number of decks
     */
    public int getDeckCountByUserId(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM deck WHERE user_id = :uid")
                .bind("uid", userId)
                .mapTo(Integer.class)
                .one()
        );
    }



    /**
     * Retrieves all decks of a user (by UUID) as a map keyed by {@link DeckCardCategory}.
     *
     * @param userId the user's UUID
     * @return a {@link HashMap} mapping deck types to {@link Deck} objects
     */
    public HashMap<DeckCardCategory, Deck> getDecksMap(UUID userId) {
        HashMap<DeckCardCategory, Deck> map = new HashMap<>();
        List<String> jsons = jdbi.withHandle(handle ->
            handle.createQuery("SELECT data FROM deck WHERE user_id = :id")
                .bind("id", userId)
                .mapTo(String.class)
                .list()
        );

        for (String json : jsons) {
            try {
                Deck deck = mapper.readValue(json, Deck.class);
                // On prend le premier DeckCardType de la HashMap comme clé
                if (!deck.getCardTabKey().isEmpty()) {
                    DeckCardCategory key = deck.getCardTabKey().keySet().iterator().next();
                    map.put(key, deck);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * Retrieves all decks of a user (by email) as a map keyed by {@link DeckCardCategory}.
     *
     * @param email the user's email
     * @return a {@link HashMap} mapping deck types to {@link Deck} objects
     */
    public HashMap<DeckCardCategory, Deck> getDecksMap(String email) {
        HashMap<DeckCardCategory, Deck> map = new HashMap<>();
        List<String> jsons = jdbi.withHandle(handle ->
            handle.createQuery(
                    "SELECT d.data FROM deck d JOIN \"user\" u ON d.user_id = u.id WHERE u.email = :email"
                )
                .bind("email", email)
                .mapTo(String.class)
                .list()
        );

        for (String json : jsons) {
            try {
                Deck deck = mapper.readValue(json, Deck.class);
                if (!deck.getCardTabKey().isEmpty()) {
                    DeckCardCategory key = deck.getCardTabKey().keySet().iterator().next();
                    map.put(key, deck);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * Retrieves a {@link UserData} object for the specified email, including
     * the user's UUID, username, and all deck data.
     *
     * @param email the user's email
     * @return the populated {@link UserData} object
     */
    public UserData getUserDataByEmail(String email) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT \"user\".id, username, data " +
                "FROM \"user\" " +
                "LEFT JOIN deck ON \"user\".id = deck.user_id " +
                "WHERE \"user\".email = :email";

            return handle.createQuery(sql)
                .bind("email", email)
                .map((rs, ctx) -> {
                    UUID uuid = (UUID) rs.getObject("id");
                    String username = rs.getString("username");
                    String decksJson = rs.getString("data");
                    HashMap<String, Deck> decks = JsonUtils.parseDecksJson(decksJson);
                    return new UserData(uuid, username, decks);
                })
                .one();
        });
    }

    /**
     * Retrieves a {@link UserData} object for the specified UUID, including
     * the user's username and all deck data.
     *
     * @param uuid the user's UUID
     * @return the populated {@link UserData} object
     */
    public UserData getUserDataByUUID(UUID uuid) {
        return jdbi.withHandle(handle -> {
            // Requête unique pour récupérer username et decks JSON
            String sql = "SELECT username, data " +
                "FROM \"user\" " +
                "LEFT JOIN deck ON \"user\".id = deck.user_id " +
                "WHERE \"user\".id = :uuid";

            return handle.createQuery(sql)
                .bind("uuid", uuid)
                .map((rs, ctx) -> {
                    String username = rs.getString("username");
                    // Supposons que data est stocké comme JSONB et que tu peux le parser en HashMap
                    String decksJson = rs.getString("data");
                    HashMap<String, Deck> decks = parseDecksJson(decksJson);
                    return new UserData(uuid, username, decks);
                })
                .one();
        });
    }



}
