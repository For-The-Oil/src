package io.github.server.server_engine.manager;

import com.esotericsoftware.kryonet.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.text.html.parser.Entity;

import io.github.server.data.network.ServerNetwork;
import io.github.server.server_engine.factory.KryoMessagePackager;
import io.github.server.server_engine.factory.RequestFactory;
import io.github.server.server_engine.utils.JsonUtils;
import io.github.shared.data.EnumsTypes.DeckCardCategory;
import io.github.shared.data.EnumsTypes.EntityType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.local.data.EnumsTypes.DeckRequestType;
import io.github.shared.data.network.KryoMessage;

public final class DeckManager {

    private static final DeckManager INSTANCE = new DeckManager();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static DeckManager getInstance() {
        return INSTANCE;
    }

    public void handleDeckRequest(Connection connection, DeckRequest request) {
        System.out.println("=== DeckRequest Received ===");
        System.out.println("Connection: " + connection);

        if (request == null) {
            System.out.println("DeckRequest is null!");
            return;
        }

        // Log du type de requête
        DeckRequestType type = request.getMode();
        System.out.println("DeckRequestType: " + type);

        // Log de toutes les clés
        if (request.getKeys() != null) {
            System.out.println("Keys in request:");
            request.getKeys().forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        } else {
            System.out.println("Keys map is null.");
        }

        // Log du client
        ClientNetwork client = ServerNetwork.getInstance().getClientByConnection(connection);
        System.out.println("Client resolved: " + client);

        if (client == null) {
            System.out.println("Client not found for this connection!");
            return;
        }

        // Log des decks du client avant traitement
        System.out.println("Client decks before request: " + client.getDecks());

        // Switch pour traiter le type
        switch (type) {
            case DELETE_DECK:
                System.out.println("Handling DELETE_DECK request...");
                deleteDeck(client, request);
                break;

            case CHANGE_CURRENT_DECK:
                System.out.println("Handling CHANGE_CURRENT_DECK request...");
                selectDeck(client, request);
                break;

            case CREATE_DECK:
                System.out.println("Handling CREATE_DECK request...");
                createDeck(client, request);
                break;

            case MODIFY_DECK:
                System.out.println("Handling MODIFY_DECK request...");
                modifyDeck(client, request);
                break;

            default:
                System.out.println("Unknown DeckRequestType: " + type);
                break;
        }

        // Log des decks du client après traitement
        System.out.println("Client decks after request: " + client.getDecks());
        System.out.println("=== End DeckRequest ===");
    }



    public void deleteDeck(ClientNetwork client, DeckRequest request) {
        if (!canDeleteDeck(client, request)) return;

        String deckToDelete = request.getKeys().getOrDefault("deck_to_delete", null);
        if (deckToDelete == null || !client.getDecks().containsKey(deckToDelete)) return;

        if (client.getDeck(deckToDelete) == client.getCurrentDeck()) {
            client.setCurrent(null);
        }

        client.getDecks().remove(deckToDelete);

        // Envoie la mise à jour au client
        sendUpdatedDecks(client);
    }




    public void modifyDeck(ClientNetwork client, DeckRequest request) {
        if (!canModifyDeck(client, request)) return;

        String deckToModify = request.getKeys().get("deck_to_modify");
        if (deckToModify == null || !client.getDecks().containsKey(deckToModify)) return;

        String deckData = request.getKeys().getOrDefault("data", null);
        if (deckData == null || deckData.isEmpty()) return;

        Deck modifiedDeck = JsonUtils.parseDeckJson(deckData);
        if (modifiedDeck == null || !isValidModifyDeck(modifiedDeck)) return;

        client.getDecks().put(deckToModify, modifiedDeck);

        // Envoie la mise à jour au client
        sendUpdatedDecks(client);
    }

    public void createDeck(ClientNetwork client, DeckRequest request) {
        if (!canCreateDeck(client, request)) return;

        // Récupère le nom du nouveau deck
        String deckName = request.getKeys().getOrDefault("deck_name", null);
        if (deckName == null || client.getDecks().containsKey(deckName)) {
            System.out.println("Deck name is invalid or already exists.");
            return;
        }

        // Crée un nouveau deck vide avec les 3 catégories initialisées
        Deck newDeck = new Deck();

        // Crée un HashMap pour les catégories
        HashMap<DeckCardCategory, ArrayList<EntityType>> cardsByCategory = new HashMap<>();
        cardsByCategory.put(DeckCardCategory.Industrial, new ArrayList<>());
        cardsByCategory.put(DeckCardCategory.Defense, new ArrayList<>());
        cardsByCategory.put(DeckCardCategory.Military, new ArrayList<>());

        // Assigne au deck
        newDeck.setCardsByCategory(cardsByCategory);


        // Ajoute le deck au client
        client.getDecks().put(deckName, newDeck);

        // Définit le deck comme courant
        client.setCurrent(newDeck);

        // Envoie la liste mise à jour au client
        sendUpdatedDecks(client);

        System.out.println("Created new deck: " + deckName);
    }




    public void selectDeck(ClientNetwork client, DeckRequest request) {
        if (!canSelectDeck(client, request)) return;

        String deckName = request.getKeys().getOrDefault("deck_name", null);
        if (deckName == null || !client.getDecks().containsKey(deckName)) {
            System.out.println("Deck does not exist.");
            return;
        }

        // Définit le deck sélectionné comme courant
        client.setCurrent(client.getDeck(deckName));

        // Envoie la liste mise à jour au client (y compris le deck courant)
        sendUpdatedDecks(client);
    }




    /**
     * Envoie au client la liste complète de ses decks après modification/suppression/création
     */
    private void sendUpdatedDecks(ClientNetwork client) {
        if (client == null || client.getDecks() == null) return;

        // Convertit tous les decks en JSON
        String decksJson = JsonUtils.toJson(new HashMap<>(client.getDecks()));

        // Sérialise les cartes débloquées en JSON
        String unlockedCardsJson = JsonUtils.toJsonUnlockedCards(client.getUnlockedCards());

        // Récupère le nom du deck courant
        String currentDeckName = null;
        if (client.getCurrentDeck() != null) {
            for (Map.Entry<String, Deck> entry : client.getDecks().entrySet()) {
                if (entry.getValue() == client.getCurrentDeck()) {
                    currentDeckName = entry.getKey();
                    break;
                }
            }
        }

        // Prépare la réponse
        HashMap<String, String> map = new HashMap<>();
        map.put("deck_data", decksJson);
        map.put("unlocked_cards", unlockedCardsJson);
        map.put("current_deck", currentDeckName != null ? currentDeckName : "");
        map.put("success", "true");

        DeckRequest response = RequestFactory.createDeckRequest(DeckRequestType.ALLOWED, map);
        KryoMessage kryoMessage = KryoMessagePackager.packageDeckRequest(response);
        client.getConnection().sendTCP(kryoMessage);
    }


    // TODO : Do the actual security check
    public boolean canDeleteDeck(ClientNetwork client, DeckRequest request){
        return true;
    }
    public boolean canModifyDeck(ClientNetwork client, DeckRequest request){
        return true;
    }
    public boolean canCreateDeck(ClientNetwork client, DeckRequest request){
        return true;
    }
    public boolean canSelectDeck(ClientNetwork client, DeckRequest request){
        return true;
    }
    public boolean isValidModifyDeck(Deck deck){
        return true;
    }


}
