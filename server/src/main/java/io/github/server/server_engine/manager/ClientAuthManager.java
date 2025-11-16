package io.github.server.server_engine.manager;

import com.esotericsoftware.kryonet.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.server.data.network.ServerNetwork;
import io.github.server.data.network.UserData;
import io.github.server.exception.BlankParameter;
import io.github.server.exception.UserEmailAlreadyExist;
import io.github.server.exception.IncorrectField;
import io.github.shared.local.data.EnumsTypes.AuthModeType;
import io.github.shared.local.data.EnumsTypes.KryoMessageType;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.network.KryoMessage;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * The {@code ClientAuthManager} class is a singleton responsible for managing all authentication
 * and user session operations on the server.
 * <p>
 * It acts as the central component of the authentication layer, ensuring that:
 * <ul>
 *     <li>User credentials and registration fields are validated properly</li>
 *     <li>Duplicate connections are avoided by maintaining a single active session per user</li>
 *     <li>All authentication-related interactions with the database are managed consistently</li>
 * </ul>
 *
 * <h3>Main Responsibilities:</h3>
 * <ol>
 *     <li>Handle user login (via email/password or UUID/token)</li>
 *     <li>Process user registration requests</li>
 *     <li>Manage authenticated clients and sessions</li>
 *     <li>Enforce credential and form validation</li>
 *     <li>Integrate with {@link DatabaseManager} for persistence and user data retrieval</li>
 * </ol>
 *
 * This design ensures that all authentication logic remains centralized, maintainable,
 * and consistent across the server lifecycle.
 */
public final class ClientAuthManager {

    /** Singleton instance of {@link ClientAuthManager}. */
    private static final ClientAuthManager INSTANCE = new ClientAuthManager();

    /** Database manager for interacting with the persistent user store. */
    private final DatabaseManager myDatabase = DatabaseManager.getInstance();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Returns the singleton instance of the {@link ClientAuthManager}.
     *
     * @return the global {@code ClientAuthManager} instance
     */
    public static ClientAuthManager getInstance() {
        return INSTANCE;
    }

    /** Private constructor to enforce the singleton pattern. */
    private ClientAuthManager() { }

    /**
     * Handles a login attempt from a client by validating credentials and
     * adding the user to the authenticated client list if successful.
     * <p>
     * Steps performed:
     * <ol>
     *     <li>Extracts login credentials (email and password) from the AuthRequest object.</li>
     *     <li>Validates the credentials using validateLogin().</li>
     *     <li>If credentials are invalid, sends an error message back to the client.</li>
     *     <li>If login is successful:
     *          <ul>
     *              <li>Adds the user to the authenticated clients list (creates a session).</li>
     *              <li>Generates a session token for auto-login / auto-connect functionality.</li>
     *              <li>Serializes the user's decks to JSON for transfer to the client.</li>
     *              <li>Prepares a response HashMap containing something like:
     *                  <ul>
     *                      <li>message: success / other</li>
     *                      <li>username: for display on the client</li>
     *                      <li>decks: JSON string of the user's decks</li>
     *                      <li>token: session token for auto-login / auto-connect</li>
     *                  </ul>
     *              </li>
     *          </ul>
     *     </li>
     *     <li>Sends the response back to the client using respondToClient().</li>
     * </ol>
     *
     * <p>
     * Important: The generated <b>token</b> allows the client to implement an <b>auto-connect</b> or
     * persistent login mechanism. The client can store this token locally (e.g., SharedPreferences on Android)
     * and use it for automatic re-authentication in future sessions without requiring the user to enter credentials again.
     *
     * @param connection the network connection of the client
     * @param object the {@link AuthRequest} containing login credentials
     */
    public void loginClient(Connection connection, AuthRequest object) {
        System.out.println("Client is asking for a login ...");
        System.out.println(object.getKeys());

        String email = object.getKeys().get("email");
        String password = object.getKeys().get("password");

        HashMap<String, String> response = new HashMap<>();

        if (!validateLogin(email, password)) {
            System.out.println("Invalid credentials!");
            response.put("message", "Invalid credentials!");
            respondToClient(connection, false, response, AuthModeType.LOGIN_FAIL);
            scheduler.schedule(connection::close, 200, TimeUnit.MILLISECONDS);
            return;
        }

        ClientNetwork client = addClient(email, connection);
        client.setIp(connection.getRemoteAddressTCP().toString());
        System.out.println("User logged in successfully: " + client);

        ObjectMapper mapper = new ObjectMapper();
        String decksJson = "{}";
        String unlockedJson = "{}";
        try {
            decksJson = mapper.writeValueAsString(client.getDecks());
            unlockedJson = mapper.writeValueAsString(client.getUnlockedCards());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(client.getUnlockedCards());
        System.out.println("Carte débloqué format JSON :" + unlockedJson);

        response.put("message", "Success");
        response.put("username", client.getUsername());
        response.put("decks", decksJson);
        response.put("unlocked", unlockedJson);
        response.put("token", client.getToken());

        respondToClient(connection, true, response, AuthModeType.LOGIN_SUCCESS);
    }

    /**
     * Handles a login attempt using a session token.
     * <p>
     * This method is a placeholder for token-based authentication logic
     * (e.g., automatic re-login or persistent session restoration).
     *
     * @param connection the network connection of the client
     * @param object the {@link AuthRequest} containing the session token
     */
    public void loginByTokenClient(Connection connection, AuthRequest object) {
        System.out.println("Client is asking for a login by token ...");
        System.out.println(object.getKeys());
        // TODO: Implement token-based login logic
    }

    /**
     * Handles the registration process for a new user.
     * <p>
     * Steps performed:
     * <ol>
     *     <li>Extracts registration fields from the AuthRequest object (email, password, password2, username).</li>
     *     <li>Validates the registration data using validateRegistration().</li>
     *     <li>If validation passes, registers the user in the database.</li>
     *     <li>Adds the newly registered user to the server's authenticated clients list (creates a session).</li>
     *     <li>Generates a session token and serializes the user's decks to JSON for transfer to the client.</li>
     *     <li>Prepares a response HashMap containing something like:
     *          <ul>
     *              <li>message: success / other</li>
     *              <li>username: for display on the client</li>
     *              <li>deck(s): JSON string of the user's decks</li>
     *              <li>token: session token </li>
     *          </ul>
     *     </li>
     *     <li>Sends the response back to the client using respondToClient().</li>
     * </ol>
     *
     * <p>
     * Important: The generated <b>token</b> allows the client to implement an <b>auto-connect</b> or
     * persistent login mechanism. The client can store this token locally (e.g., SharedPreferences on Android)
     * and use it for automatic re-authentication without requiring the user to enter credentials again.
     *
     * @param connection the client's network connection
     * @param object the {@link AuthRequest} containing registration fields
     */
    public void registerClient(Connection connection, AuthRequest object) {
        System.out.println("Client is asking to register ...");
        System.out.println(object.getKeys());

        HashMap<String, String> mymap = object.getKeys();

        String email = mymap.get("email");
        String password = mymap.get("password");
        String password2 = mymap.get("password2");
        String username = mymap.get("username");

        HashMap<String, String> response = new HashMap<>();

        try {
            validateRegistration(email, password, password2, username);
            myDatabase.registerUser(email, username, password);
            System.out.print("Client successfully registered...");




            ClientNetwork client = addClient(email, connection);

            ObjectMapper mapper = new ObjectMapper();
            String decksJson = "{}";
            String unlockedJson = "{}";
            try {
                decksJson = mapper.writeValueAsString(client.getDecks());
                unlockedJson = mapper.writeValueAsString(client.getUnlockedCards());
            } catch (Exception e) {
                e.printStackTrace();
            }

            response.put("message", "Success");
            response.put("username", client.getUsername());
            response.put("decks", decksJson);
            response.put("unlocked", unlockedJson);
            response.put("token", client.getToken());

            respondToClient(connection, true, response, AuthModeType.REGISTER_SUCCESS);



        } catch (BlankParameter | IncorrectField | UserEmailAlreadyExist e) {
            System.err.println(e);
            response.put("message", e.toString());
            respondToClient(connection, false, response, AuthModeType.REGISTER_FAIL);
            scheduler.schedule(connection::close, 200, TimeUnit.MILLISECONDS);
        }

    }

    /**
     * Validates registration input fields.
     * <p>
     * Ensures that all provided fields meet formatting, non-null, and uniqueness constraints.
     *
     * @param email the email address provided by the user
     * @param password the password provided by the user
     * @param password2 the repeated password confirmation
     * @param username the username chosen by the user
     * @throws UserEmailAlreadyExist if the email is already registered
     * @throws IncorrectField if the input data fails validation
     * @throws BlankParameter if any field is left empty
     */
    public void validateRegistration(String email, String password, String password2, String username)
        throws UserEmailAlreadyExist, IncorrectField, BlankParameter {

        if (email == null || email.isEmpty())
            throw new BlankParameter("Email cannot be empty!");
        if (password == null || password.isEmpty() || password2 == null || password2.isEmpty())
            throw new BlankParameter("Password fields cannot be empty!");
        if (!password.equals(password2))
            throw new IncorrectField("Passwords do not match!");
        if (username == null || username.isEmpty())
            throw new BlankParameter("Username cannot be empty!");
        if (!isUserEmailValid(email))
            throw new IncorrectField("Email format is invalid!");
        if (!isUserUsernameValid(username))
            throw new IncorrectField("Username is invalid!");
        if (!isPasswordValid(password))
            throw new IncorrectField("Password is invalid!");
        if (myDatabase.userExists(email))
            throw new UserEmailAlreadyExist("Email already registered!");
    }

    /**
     * Adds an authenticated client to the server's internal list using a UUID.
     * <p>
     * If a client with the same UUID is already connected, the existing connection
     * is closed and replaced with the new one.
     *
     * @param uuid the UUID of the authenticated user
     * @param connection the user's network connection
     * @return a {@link ClientNetwork} instance representing the authenticated client
     * @throws RuntimeException if the user data could not be retrieved
     */
    public ClientNetwork addClient(UUID uuid, Connection connection) {
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        UserData userData = db.getUserDataByUUID(uuid);
        if (userData == null)
            throw new RuntimeException("User with UUID not found in DB");

        ClientNetwork existingClient = serverNetwork.getAuthClientNetworkList().stream()
            .filter(c -> c.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);

        if (existingClient != null) {
            serverNetwork.getAuthClientNetworkList().remove(existingClient);
            System.out.println("Removed previous connection for user: " + existingClient.getUsername());
        }

        String sessionToken = UUID.randomUUID().toString();

        ClientNetwork client = new ClientNetwork(
            userData.getUuid(),
            userData.getUsername(),
            userData.getDecks(),
            userData.getUnlockedCards(),
            sessionToken,
            connection
        );

        serverNetwork.getAuthClientNetworkList().add(client);
        return client;
    }

    /**
     * Adds an authenticated client using their email address.
     * <p>
     * Performs a similar process to {@link #addClient(UUID, Connection)}, but identifies
     * the user by email instead of UUID.
     *
     * @param email the user's email address
     * @param connection the user's network connection
     * @return a {@link ClientNetwork} instance representing the authenticated client
     * @throws RuntimeException if the user data could not be retrieved
     */
    public ClientNetwork addClient(String email, Connection connection) {
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        UserData userData = db.getUserDataByEmail(email);
        if (userData == null)
            throw new RuntimeException("User with email not found in DB");

        ClientNetwork existingClient = serverNetwork.getAuthClientNetworkList().stream()
            .filter(c -> c.getUuid().equals(userData.getUuid()))
            .findFirst()
            .orElse(null);

        if (existingClient != null) {
            serverNetwork.getAuthClientNetworkList().remove(existingClient);
            System.out.println("Removed previous connection for user: " + existingClient.getUsername());
        }

        String sessionToken = UUID.randomUUID().toString();

        ClientNetwork client = new ClientNetwork(
            userData.getUuid(),
            userData.getUsername(),
            userData.getDecks(),
            userData.getUnlockedCards(),
            sessionToken,
            connection
        );

        serverNetwork.getAuthClientNetworkList().add(client);
        return client;
    }

    /**
     * Validates the provided login credentials against the database.
     *
     * @param email the user's email
     * @param password the user's password
     * @return {@code true} if credentials are valid; {@code false} otherwise
     */
    public boolean validateLogin(String email, String password) {
        if (!myDatabase.userExists(email))
            return false;
        return myDatabase.login(email, password);
    }

    /**
     * Placeholder methods for input validation logic.
     * <p>
     * In production, these methods should enforce:
     * <ul>
     *     <li>Strong password policies</li>
     *     <li>Email format validation (e.g., regex)</li>
     *     <li>Username character and length constraints</li>
     * </ul>
     */
    public boolean isPasswordValid(String password) { return true; }
    public boolean isUserEmailValid(String email) { return true; }
    public boolean isUserUsernameValid(String username) { return true; }



    /**
     * Envoie une réponse d'authentification au client.
     *
     * @param connection Le client à notifier
     * @param success    true si l'action a réussi, false sinon
     * @param messages   Map contenant des clés/valeurs d'information à afficher côté client (ex: "message", "reason", "username", "token", etc.)
     */
    public void respondToClient(Connection connection, boolean success, HashMap<String, String> messages, AuthModeType type) {
        if (connection == null) return;

        // Assure que le dictionnaire existe
        if (messages == null) messages = new HashMap<>();

        // Ajoute un flag "success" systématiquement
        messages.put("success", Boolean.toString(success));

        AuthModeType mode = type;

        // Crée l'AuthRequest contenant les informations à transmettre
        AuthRequest authResponse = new AuthRequest(mode, messages);

        // Emballe dans un KryoMessage pour KryoNet
        KryoMessage kryoMessage = new KryoMessage(KryoMessageType.AUTH, null, authResponse);

        // Envoie le message au client
        connection.sendTCP(kryoMessage);
    }



    /** Removes a connected client from the authenticated list (TODO: implementation pending). */
    public void removeClient(Connection connection) {
        if (connection == null) return;
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByConnection(connection);
        removeClient(connection,client);
    }

    public void removeClient(String username) {
        if (username == null || username.isEmpty()) return;
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByUsername(username);
        removeClient(client.getConnection(), client);
    }

    public void removeClient(UUID uuid) {
        if (uuid == null || uuid.toString().isEmpty()) return;
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        ClientNetwork client = serverNetwork.getClientByUUID(uuid);
        removeClient(client.getConnection(), client);
    }

    private void removeClient(Connection connection,ClientNetwork client){
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        if (client != null) {
            // Retire le client de la liste
            serverNetwork.getAuthClientNetworkList().remove(client);

            // Ferme la connexion TCP
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture de la connexion pour " + client.getUsername());
                e.printStackTrace();
            }

            System.out.println("Client déconnecté : " + client.getUsername() + " (" + connection.getID() + ")");
        }
    }



    /** Deletes a user account by email (TODO: implementation pending). */
    public void deleteClient(String email) { /* TODO: implement client deletion from DB */ }

    /** Deletes a user account by UUID (TODO: implementation pending). */
    public void deleteClient(UUID uuid) { /* TODO: implement client deletion from DB */ }

}
