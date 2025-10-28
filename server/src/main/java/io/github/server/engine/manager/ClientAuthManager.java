package io.github.server.engine.manager;

import com.esotericsoftware.kryonet.Connection;
import java.util.HashMap;
import java.util.UUID;
import io.github.server.data.network.ServerNetwork;
import io.github.server.data.network.UserData;
import io.github.server.exception.BlankParameter;
import io.github.server.exception.UserEmailAlreadyExist;
import io.github.server.exception.IncorrectField;
import io.github.server.network.DatabaseManager;
import io.github.shared.local.data.network.ClientNetwork;
import io.github.shared.local.data.requests.AuthRequest;

/**
 * ClientAuthManager is a singleton responsible for handling all operations related to user accounts and sessions on the server.
 *
 * Responsibilities include:
 * 1. User login by email or UUID
 * 2. User registration
 * 3. Validation of credentials and registration forms
 * 4. Adding users to the authenticated clients list
 * 5. Managing session tokens and duplicate connections
 * 6. Integration with DatabaseManager to fetch and validate user data
 *
 * The class keeps the server-side logic for user sessions centralized, ensuring
 * that no duplicate client sessions exist and that all credentials are properly validated.
 */
public final class ClientAuthManager {

    // Singleton instance
    private static final ClientAuthManager INSTANCE = new ClientAuthManager();

    // Database manager instance for accessing user data
    private final DatabaseManager myDatabase = DatabaseManager.getInstance();

    /**
     * Returns the singleton instance of ClientAuthManager.
     * @return ClientAuthManager instance
     */
    public static ClientAuthManager getInstance() {
        return INSTANCE;
    }

    // Private constructor to enforce singleton pattern
    private ClientAuthManager() { }

    /**
     * Handles user login by email and password.
     * Validates credentials and adds the client to the authenticated clients list.
     *
     * @param connection the network connection of the client
     * @param object the authentication request containing email and password
     */
    public void loginClient(Connection connection, AuthRequest object) {
        System.out.println("Client is asking for a login ...");
        System.out.println(object.getKeys());

        String email = object.getKeys().get("email");
        String password = object.getKeys().get("password");

        // Validate credentials
        if (!validateLogin(email, password)) {
            System.out.println("Invalid credentials!");
            return;
        }

        // Add client to authenticated list
        ClientNetwork client = addClient(email, connection);
        System.out.println("User logged in successfully: " + client);
    }

    /**
     * Handles user login by session token (future implementation).
     *
     * @param connection the network connection of the client
     * @param object the authentication request containing token
     */
    public void loginByTokenClient(Connection connection, AuthRequest object){
        System.out.println("Client is asking for a login by token ...");
        System.out.println(object.getKeys());
        /* TODO : implements the actual logic behind */
    }

    /**
     * Handles user registration.
     * Validates registration data, registers the user in the database, and adds the client to the authenticated list.
     *
     * @param connection the network connection of the client
     * @param object the authentication request containing registration info
     */
    public void registerClient(Connection connection, AuthRequest object){
        System.out.println("Client is asking to register ...");
        System.out.println(object.getKeys());

        HashMap<String,String> mymap =  object.getKeys();

        String email = mymap.get("email");
        String password = mymap.get("password");
        String password2 = mymap.get("password2");
        String username = mymap.get("username");

        try {
            // Validate registration fields
            validateRegistration(email, password, password2, username);

            // Register user in the database
            myDatabase.registerUser(email, username, password);

            // Add client to authenticated list
            addClient(email, connection);

        } catch (BlankParameter | IncorrectField | UserEmailAlreadyExist e){
            System.err.println(e);
        }
    }

    /**
     * Validates a registration form, checking for empty fields, format, and uniqueness in the database.
     *
     * @param email user email
     * @param password user password
     * @param password2 repeated password for confirmation
     * @param username desired username
     * @throws UserEmailAlreadyExist if email is already registered
     * @throws IncorrectField if any field is invalid
     * @throws BlankParameter if any field is empty
     */
    public void validateRegistration(String email, String password, String password2, String username)
        throws UserEmailAlreadyExist, IncorrectField, BlankParameter {

        if (email == null || email.isEmpty()) throw new BlankParameter("Email cannot be empty!");
        if (password == null || password.isEmpty() || password2 == null || password2.isEmpty())
            throw new BlankParameter("Password fields cannot be empty!");
        if (!password.equals(password2)) throw new IncorrectField("Passwords do not match!");
        if (username == null || username.isEmpty()) throw new BlankParameter("Username cannot be empty!");
        if (!isUserEmailValid(email)) throw new IncorrectField("Email format is invalid!");
        if (!isUserUsernameValid(username)) throw new IncorrectField("Username is invalid!");
        if (!isPasswordValid(password)) throw new IncorrectField("Password is invalid!");
        if (myDatabase.userExists(email)) throw new UserEmailAlreadyExist("Email already registered!");
    }

    /**
     * Adds a client to the authenticated clients list by UUID.
     * Removes any existing session for the same UUID to prevent duplicates.
     * Generates a new session token.
     *
     * @param uuid the UUID of the user
     * @param connection the user's network connection
     * @return the created ClientNetwork object
     */
    public ClientNetwork addClient(UUID uuid, Connection connection) {
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        // Retrieve all user data in a single DB call
        UserData userData = db.getUserDataByUUID(uuid);
        if (userData == null) throw new RuntimeException("User with UUID not found in DB");

        // Remove old connection if client already exists
        ClientNetwork existingClient = serverNetwork.getAuthClientNetworkList().stream()
            .filter(c -> c.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);

        if (existingClient != null) {
            serverNetwork.getAuthClientNetworkList().remove(existingClient);
            System.out.println("Removed previous connection for user: " + existingClient.getUsername());
        }

        // Generate a session token
        String sessionToken = UUID.randomUUID().toString();

        ClientNetwork client = new ClientNetwork(
            userData.getUuid(),
            userData.getUsername(),
            userData.getDecks(),
            sessionToken,
            connection
        );

        serverNetwork.getAuthClientNetworkList().add(client);
        return client;
    }

    /**
     * Adds a client to the authenticated clients list by email.
     * Removes any existing session for the same UUID to prevent duplicates.
     * Generates a new session token.
     *
     * @param email the email of the user
     * @param connection the user's network connection
     * @return the created ClientNetwork object
     */
    public ClientNetwork addClient(String email, Connection connection) {
        ServerNetwork serverNetwork = ServerNetwork.getInstance();
        DatabaseManager db = DatabaseManager.getInstance();

        // Retrieve all user data in a single DB call
        UserData userData = db.getUserDataByEmail(email);
        if (userData == null) throw new RuntimeException("User with email not found in DB");

        // Remove old connection if client already exists
        ClientNetwork existingClient = serverNetwork.getAuthClientNetworkList().stream()
            .filter(c -> c.getUuid().equals(userData.getUuid()))
            .findFirst()
            .orElse(null);

        if (existingClient != null) {
            serverNetwork.getAuthClientNetworkList().remove(existingClient);
            System.out.println("Removed previous connection for user: " + existingClient.getUsername());
        }

        // Generate a session token
        String sessionToken = UUID.randomUUID().toString();

        ClientNetwork client = new ClientNetwork(
            userData.getUuid(),
            userData.getUsername(),
            userData.getDecks(),
            sessionToken,
            connection
        );

        serverNetwork.getAuthClientNetworkList().add(client);
        return client;
    }

    /**
     * Validates login credentials by checking the database for email existence and password correctness.
     *
     * @param email user's email
     * @param password user's password
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateLogin(String email, String password) {
        if (!myDatabase.userExists(email)) return false;
        return myDatabase.login(email, password);
    }

    /**
     * Stub methods for validation rules.
     * Replace with real validation logic as needed.
     */
    public boolean isPasswordValid(String password){ return true; }
    public boolean isUserEmailValid(String email){ return true; }
    public boolean isUserUsernameValid(String username){ return true; }

    public void removeClient(){ /* TODO: implement client removal */ }


    public void deleteClient(String email){/* TODO: implement client suppression from the DB */}

    public void deleteClient(UUID uuid){/* TODO: implement client suppression from the DB */}

}
