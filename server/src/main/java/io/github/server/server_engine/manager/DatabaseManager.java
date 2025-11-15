package io.github.server.server_engine.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.server.config.DatabaseConfig;
import io.github.server.data.network.UserData;
import io.github.server.server_engine.utils.DefaultDeckGenerator;
import io.github.server.server_engine.utils.JsonUtils;
import io.github.shared.local.data.EnumsTypes.DeckCardCategory;
import io.github.shared.local.data.EnumsTypes.EntityType;
import io.github.shared.local.data.gameobject.Deck;
import org.mindrot.jbcrypt.BCrypt;
import org.jdbi.v3.core.Jdbi;

import java.util.*;

/**
 * Simplified database manager for users, collections, battles and stats.
 */
public final class DatabaseManager {

    // -------------------------
    // Table & Column Constants
    // -------------------------
    private static final String TABLE_USERS = "users";
    private static final String TABLE_USER_COLLECTIONS = "user_collections";
    private static final String TABLE_BATTLES = "battles";
    private static final String TABLE_USER_BATTLES = "user_battles";
    private static final String TABLE_USER_STATS = "user_stats";

    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_UNLOCKED_CARDS = "unlocked_cards";
    private static final String COL_DECKS = "decks";
    private static final String COL_LAST_UPDATE = "last_update";
    private static final String COL_BATTLE_ID = "battle_id";
    private static final String COL_WINS = "wins";
    private static final String COL_LOSSES = "losses";

    // -------------------------
    // Singleton / JDBI / Mapper
    // -------------------------
    private final ObjectMapper mapper = new ObjectMapper();
    private static DatabaseManager INSTANCE;
    private final Jdbi jdbi;

    private DatabaseManager() {
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
        config.setMaximumPoolSize(5);
        config.setDriverClassName("org.postgresql.Driver");
        HikariDataSource dataSource = new HikariDataSource(config);
        this.jdbi = Jdbi.create(dataSource);
    }

    public static DatabaseManager getInstance() {
        if (INSTANCE == null) INSTANCE = new DatabaseManager();
        return INSTANCE;
    }

    // ==========================
    // USERS
    // ==========================
    public boolean registerUser(String email, String username, String password) {
        if (userExists(email)) return false;

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        UUID userId = UUID.randomUUID();

        // -------------------------
        // Deck par défaut
        // -------------------------
        Map<String, Deck> defaultDeckMap = DefaultDeckGenerator.generateDefaultDeck();

        // -------------------------
        // Cartes débloquées initiales
        // -------------------------
        List<EntityType> unlockedCards = new ArrayList<>();
        for (Deck deck : defaultDeckMap.values()) {
            for (ArrayList<EntityType> list : deck.getCardTabKey().values()) {
                for (EntityType entity : list) {
                    if (!unlockedCards.contains(entity)) {
                        unlockedCards.add(entity);
                    }
                }
            }
        }

        // -------------------------
        // Sérialisation JSON
        // -------------------------
        String unlockedCardsJson;
        String decksJson;
        try {
            unlockedCardsJson = mapper.writeValueAsString(unlockedCards);
            decksJson = mapper.writeValueAsString(defaultDeckMap);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // -------------------------
        // Transaction DB
        // -------------------------
        jdbi.useTransaction(handle -> {
            // Création de l'utilisateur
            handle.execute(
                "INSERT INTO " + TABLE_USERS + " (" + COL_ID + ", " + COL_EMAIL + ", " + COL_USERNAME + ", " + COL_PASSWORD + ") VALUES (?, ?, ?, ?)",
                userId, email, username, hash
            );

            // Création des collections de l'utilisateur
            handle.execute(
                "INSERT INTO " + TABLE_USER_COLLECTIONS + " (" + COL_USER_ID + ", " + COL_UNLOCKED_CARDS + ", " + COL_DECKS + ") " +
                    "VALUES (?, CAST(? AS JSONB), CAST(? AS JSONB))",
                userId, unlockedCardsJson, decksJson
            );

            // Initialisation des stats utilisateur
            handle.execute(
                "INSERT INTO " + TABLE_USER_STATS + " (" + COL_USER_ID + ", " + COL_WINS + ", " + COL_LOSSES + ") VALUES (?, 0, 0)",
                userId
            );
        });

        return true;
    }



    public boolean login(String email, String password) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_PASSWORD + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = :email")
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .map(hash -> BCrypt.checkpw(password, hash))
                .orElse(false)
        );
    }

    public boolean login(UUID id, String password) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_PASSWORD + " FROM " + TABLE_USERS + " WHERE " + COL_ID + " = :id")
                .bind("id", id)
                .mapTo(String.class)
                .findOne()
                .map(hash -> BCrypt.checkpw(password, hash))
                .orElse(false)
        );
    }

    public boolean userExists(String email) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = :email")
                .bind("email", email)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }

    public boolean userExists(UUID id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_ID + " = :id")
                .bind("id", id)
                .mapTo(Integer.class)
                .one()
        ) > 0;
    }

    private Optional<UUID> getUserIdByEmail(String email) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = :email")
                .bind("email", email)
                .mapTo(UUID.class)
                .findOne()
        );
    }

    // ==========================
    // USER COLLECTIONS
    // ==========================
    public Optional<String> getUnlockedCardsJson(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_UNLOCKED_CARDS + "::text FROM " + TABLE_USER_COLLECTIONS + " WHERE " + COL_USER_ID + " = :uid")
                .bind("uid", userId)
                .mapTo(String.class)
                .findOne()
        );
    }

    public Optional<String> getDecksJson(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_DECKS + "::text FROM " + TABLE_USER_COLLECTIONS + " WHERE " + COL_USER_ID + " = :uid")
                .bind("uid", userId)
                .mapTo(String.class)
                .findOne()
        );
    }

    public boolean updateUnlockedCards(UUID userId, String jsonData) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE " + TABLE_USER_COLLECTIONS + " SET " + COL_UNLOCKED_CARDS + " = CAST(:data AS JSONB), " + COL_LAST_UPDATE + " = NOW() WHERE " + COL_USER_ID + " = :uid")
                .bind("data", jsonData)
                .bind("uid", userId)
                .execute() > 0
        );
    }

    public boolean updateDecks(UUID userId, String jsonData) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE " + TABLE_USER_COLLECTIONS + " SET " + COL_DECKS + " = CAST(:data AS JSONB), " + COL_LAST_UPDATE + " = NOW() WHERE " + COL_USER_ID + " = :uid")
                .bind("data", jsonData)
                .bind("uid", userId)
                .execute() > 0
        );
    }

    // ==========================
    // BATTLES
    // ==========================
    public int createBattle(Date startTime, Date endTime) {
        return jdbi.withHandle(handle ->
            handle.createUpdate(
                    "INSERT INTO " + TABLE_BATTLES + " (start_time, end_time) VALUES (:start, :end) RETURNING " + COL_BATTLE_ID)
                .bind("start", startTime)
                .bind("end", endTime)
                .executeAndReturnGeneratedKeys(COL_BATTLE_ID)
                .mapTo(Integer.class)
                .one()
        );
    }

    public List<Map<String, Object>> getBattles() {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM " + TABLE_BATTLES)
                .mapToMap()
                .list()
        );
    }

    public boolean addUserToBattle(UUID userId, int battleId) {
        return jdbi.withHandle(handle -> {
            int inserted = handle.createUpdate(
                    "INSERT INTO " + TABLE_USER_BATTLES + " (" + COL_USER_ID + ", " + COL_BATTLE_ID + ") VALUES (:uid, :bid) ON CONFLICT DO NOTHING")
                .bind("uid", userId)
                .bind("bid", battleId)
                .execute();
            return inserted > 0;
        });
    }

    public List<Integer> getBattlesByUser(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_BATTLE_ID + " FROM " + TABLE_USER_BATTLES + " WHERE " + COL_USER_ID + " = :uid")
                .bind("uid", userId)
                .mapTo(Integer.class)
                .list()
        );
    }

    public List<UUID> getUsersByBattle(int battleId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USER_BATTLES + " WHERE " + COL_BATTLE_ID + " = :bid")
                .bind("bid", battleId)
                .mapTo(UUID.class)
                .list()
        );
    }

    // ==========================
    // USER STATS
    // ==========================
    public Map<String, Object> getUserStats(UUID userId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT " + COL_WINS + ", " + COL_LOSSES + " FROM " + TABLE_USER_STATS + " WHERE " + COL_USER_ID + " = :uid")
                .bind("uid", userId)
                .mapToMap()
                .findOne()
                .orElseGet(() -> {
                    Map<String, Object> defaultMap = new HashMap<>();
                    defaultMap.put(COL_WINS, 0);
                    defaultMap.put(COL_LOSSES, 0);
                    return defaultMap;
                })
        );
    }

    public boolean updateUserStats(UUID userId, int wins, int losses) {
        return jdbi.withHandle(handle -> {
            int updated = handle.createUpdate(
                    "UPDATE " + TABLE_USER_STATS + " SET " + COL_WINS + " = :wins, " + COL_LOSSES + " = :losses WHERE " + COL_USER_ID + " = :uid")
                .bind("wins", wins)
                .bind("losses", losses)
                .bind("uid", userId)
                .execute();
            if (updated > 0) return true;

            int inserted = handle.createUpdate(
                    "INSERT INTO " + TABLE_USER_STATS + " (" + COL_USER_ID + ", " + COL_WINS + ", " + COL_LOSSES + ") VALUES (:uid, :wins, :losses)")
                .bind("uid", userId)
                .bind("wins", wins)
                .bind("losses", losses)
                .execute();
            return inserted > 0;
        });
    }

    // ==========================
    // USER DATA
    // ==========================
    public UserData getUserDataByUUID(UUID uuid) {
        return jdbi.withHandle(handle -> {
            // On récupère le username et les collections JSON (décks + cartes)
            String sql = "SELECT u." + COL_USERNAME + ", c." + COL_DECKS +
                " FROM " + TABLE_USERS + " u " +
                "LEFT JOIN " + TABLE_USER_COLLECTIONS + " c ON u." + COL_ID + " = c." + COL_USER_ID +
                " WHERE u." + COL_ID + " = :uuid";

            return handle.createQuery(sql)
                .bind("uuid", uuid)
                .map((rs, ctx) -> {
                    String username = rs.getString(COL_USERNAME);
                    String decksJson = rs.getString(COL_DECKS);

                    // Convertir le JSON en HashMap<String, Deck>
                    HashMap<String, Deck> decks = JsonUtils.parseDecksJson(decksJson);

                    return new UserData(uuid, username, decks);
                })
                .findOne()
                .orElse(null); // Retourne null si l'utilisateur n'existe pas
        });
    }


    // ==========================
    // USER DATA by Email
    // ==========================
    public UserData getUserDataByEmail(String email) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT u." + COL_ID + ", u." + COL_USERNAME + ", c." + COL_DECKS +
                " FROM " + TABLE_USERS + " u " +
                "LEFT JOIN " + TABLE_USER_COLLECTIONS + " c ON u." + COL_ID + " = c." + COL_USER_ID +
                " WHERE u." + COL_EMAIL + " = :email";

            return handle.createQuery(sql)
                .bind("email", email)
                .map((rs, ctx) -> {
                    UUID uuid = (UUID) rs.getObject(COL_ID);
                    String username = rs.getString(COL_USERNAME);
                    String decksJson = rs.getString(COL_DECKS);

                    HashMap<String, Deck> decks = JsonUtils.parseDecksJson(decksJson);
                    return new UserData(uuid, username, decks);
                })
                .findOne()
                .orElse(null);
        });
    }



}
