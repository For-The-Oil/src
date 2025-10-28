package io.github.server.network;

import static io.github.server.network.JsonUtils.parseDecksJson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.Handle;
import org.mindrot.jbcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import io.github.server.config.DatabaseConfig;
import io.github.server.data.network.UserData;
import io.github.shared.local.data.EnumsTypes.DeckCardType;
import io.github.shared.local.data.gameobject.Deck;

/**
 * DatabaseManager handles PostgreSQL connection and operations on users and decks.
 *
 * Uses HikariCP for connection pooling and JDBI for database interaction.
 * Table "user" and table "deck" are assumed to exist.
 */
public class DatabaseManager {

    private final ObjectMapper mapper = new ObjectMapper();
    private static DatabaseManager INSTANCE;
    private final Jdbi jdbi;

    /**
     * Private constructor initializing the database connection pool and JDBI instance.
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
     * Returns the singleton instance of DatabaseManager.
     *
     * @return DatabaseManager instance
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
     *
     * @param email    the email of the new user (must be unique)
     * @param username the username of the new user
     * @param password the plain text password of the new user
     * @return true if the user was successfully registered, false if the email already exists
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
     * @param email    the email of the user
     * @param password the plain text password
     * @return true if credentials match, false otherwise
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
     * @param password the plain text password
     * @return true if credentials match, false otherwise
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
     * @return true if the user exists, false otherwise
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
     * @param id the UUID of the user
     * @return true if the user exists, false otherwise
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
     * Helper: get user UUID from email.
     *
     * @param email the user's email
     * @return Optional UUID of the user, or empty if not found
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
     * Return a list of deck JSON strings for the user identified by email.
     *
     * @param email the user's email
     * @return list of JSON strings (may be empty)
     */
    public List<String> getDecksJsonByEmail(String email) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return Collections.emptyList();
        return getDecksJsonByUserId(maybeId.get());
    }

    /**
     * Return a list of deck JSON strings for the user identified by UUID.
     *
     * @param userId the user's UUID
     * @return list of JSON strings (may be empty)
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
     * Return a single deck JSON (by name) for the user identified by email.
     *
     * @param email the user's email
     * @param deckName the deck name
     * @return Optional JSON string (empty if not found)
     */
    public Optional<String> getDeckJsonByEmailAndName(String email, String deckName) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return Optional.empty();
        return getDeckJsonByUserIdAndName(maybeId.get(), deckName);
    }

    /**
     * Return a single deck JSON (by name) for the user identified by UUID.
     *
     * @param userId the user's UUID
     * @param deckName the deck name
     * @return Optional JSON string (empty if not found)
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
     * Upsert (update if exists, otherwise insert) the deck JSON for a user identified by email.
     *
     * @param email the user's email
     * @param deckName the deck name
     * @param jsonData the deck JSON (string)
     * @return true if operation succeeded, false if user not found or error
     */
    public boolean setDeckJsonByEmail(String email, String deckName, String jsonData) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return false;
        return setDeckJsonByUserId(maybeId.get(), deckName, jsonData);
    }

    /**
     * Upsert (update if exists, otherwise insert) the deck JSON for a user identified by UUID.
     *
     * @param userId the user's UUID
     * @param deckName the deck name
     * @param jsonData the deck JSON (string)
     * @return true if operation succeeded, false otherwise
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
     * Returns the number of decks associated with a user identified by email.
     *
     * @param email the user's email
     * @return number of decks (0 if user not found)
     */
    public int getDeckCountByEmail(String email) {
        Optional<UUID> maybeId = getUserIdByEmail(email);
        if (!maybeId.isPresent()) return 0;
        return getDeckCountByUserId(maybeId.get());
    }

    /**
     * Returns the number of decks associated with a user identified by UUID.
     *
     * @param userId the user's UUID
     * @return number of decks
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
     * Get all decks of a user by UUID, returned as a HashMap keyed by DeckCardType.
     * @param userId the UUID of the user
     * @return HashMap of DeckCardType -> Deck
     */
    public HashMap<DeckCardType, Deck> getDecksMap(UUID userId) {
        HashMap<DeckCardType, Deck> map = new HashMap<>();
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
                    DeckCardType key = deck.getCardTabKey().keySet().iterator().next();
                    map.put(key, deck);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * Get all decks of a user by email, returned as a HashMap keyed by DeckCardType.
     * @param email the email of the user
     * @return HashMap of DeckCardType -> Deck
     */
    public HashMap<DeckCardType, Deck> getDecksMap(String email) {
        HashMap<DeckCardType, Deck> map = new HashMap<>();
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
                    DeckCardType key = deck.getCardTabKey().keySet().iterator().next();
                    map.put(key, deck);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }


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
