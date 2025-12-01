package io.github.android.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import io.github.android.gui.Card;
import io.github.android.utils.JsonUtils;
import io.github.core.client_engine.factory.KryoMessagePackager;
import io.github.core.client_engine.factory.RequestFactory;
import io.github.shared.data.enumsTypes.DeckCardCategory;
import io.github.shared.data.enumsTypes.EntityType;
import io.github.shared.data.enumsTypes.ResourcesType;
import io.github.shared.data.gameobject.Deck;
import io.github.shared.data.network.KryoMessage;
import io.github.shared.data.requests.DeckRequest;
import io.github.shared.data.enumsTypes.DeckRequestType;


public final class DeckManager {


    public static void addCard(Card card, String deckName, String activeCategory) {
        if (card == null || deckName == null || activeCategory == null) return;

        HashMap<String, String> keys = new HashMap<>();
        keys.put("deck_to_modify", deckName);

        Deck currentDeck = SessionManager.getInstance().getDecks().get(deckName);
        if (currentDeck != null) {
            try {
                // Convertit le nom en EntityType
                EntityType type = EntityType.valueOf(card.getName());
                DeckCardCategory category = DeckCardCategory.valueOf(activeCategory);

                currentDeck.addCard(type, category); // ajoute localement
                keys.put("data", JsonUtils.deckToJson(currentDeck)); // sérialise le deck complet
            } catch (IllegalArgumentException e) {
                // Nom invalide -> on ignore
                e.printStackTrace();
                return;
            }
        }
        DeckRequest request = RequestFactory.createDeckRequest(DeckRequestType.MODIFY_DECK, keys);
        KryoMessage kryoMessage = KryoMessagePackager.packDeckRequest(request, SessionManager.getInstance().getToken());

        new Thread(() -> {
            ClientManager.getInstance().getKryoManager().send(kryoMessage);
        }).start();
    }


    public static void removeCard(Card card, String deckName, String activeCategory) {
        Log.d("DeckManager", "removeCard called: card=" + card + ", deckName=" + deckName + ", activeCategory=" + activeCategory);

        if (card == null || deckName == null || activeCategory == null) {
            Log.d("DeckManager", "removeCard aborted: null parameter detected.");
            return;
        }

        HashMap<String, String> keys = new HashMap<>();
        keys.put("deck_to_modify", deckName);

        Deck currentDeck = SessionManager.getInstance().getDecks().get(deckName);
        if (currentDeck != null) {
            try {
                EntityType type = EntityType.valueOf(card.getName());
                DeckCardCategory category = DeckCardCategory.valueOf(activeCategory);

                Log.d("DeckManager", "Removing card: " + type + " from category: " + category);
                currentDeck.removeCard(type, category);
                keys.put("data", JsonUtils.deckToJson(currentDeck));
                Log.d("DeckManager", "Deck after removal: " + currentDeck.toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.d("DeckManager", "Error parsing card name or category: " + e.getMessage());
                return;
            }
        } else {
            Log.d("DeckManager", "Deck not found: " + deckName);
        }

        DeckRequest request = RequestFactory.createDeckRequest(DeckRequestType.MODIFY_DECK, keys);
        Log.d("DeckManager", "DeckRequest created: " + request);

        KryoMessage kryoMessage = KryoMessagePackager.packDeckRequest(request, SessionManager.getInstance().getToken());
        Log.d("DeckManager", "KryoMessage packaged, sending...");

        new Thread(() -> {
            Log.d("DeckManager", "Sending KryoMessage in new thread...");
            ClientManager.getInstance().getKryoManager().send(kryoMessage);
            Log.d("DeckManager", "KryoMessage sent.");
        }).start();
    }

    public static void deleteDeck(String deckName) {
        Log.d("DeckManager", "deleteDeck called: deckName=" + deckName);

        if (deckName == null) {
            Log.d("DeckManager", "deleteDeck aborted: deckName is null");
            return;
        }

        HashMap<String, String> keys = new HashMap<>();
        keys.put("deck_to_delete", deckName);

        DeckRequest request = RequestFactory.createDeckRequest(DeckRequestType.DELETE_DECK, keys);
        Log.d("DeckManager", "DeckRequest created: " + request);

        KryoMessage kryoMessage = KryoMessagePackager.packDeckRequest(request, SessionManager.getInstance().getToken());
        Log.d("DeckManager", "KryoMessage packaged, sending...");

        new Thread(() -> {
            Log.d("DeckManager", "Sending KryoMessage in new thread...");
            ClientManager.getInstance().getKryoManager().send(kryoMessage);
            Log.d("DeckManager", "KryoMessage sent.");
        }).start();
    }

    public static void createDeck(String deckName) {
        Log.d("DeckManager", "createDeck called: deckName=" + deckName);

        if (deckName == null || deckName.isEmpty()) {
            Log.d("DeckManager", "createDeck aborted: invalid deckName");
            return;
        }

        HashMap<String, String> keys = new HashMap<>();
        keys.put("deck_name", deckName);

        DeckRequest request = RequestFactory.createDeckRequest(DeckRequestType.CREATE_DECK, keys);
        Log.d("DeckManager", "DeckRequest created: " + request);

        KryoMessage kryoMessage = KryoMessagePackager.packDeckRequest(request, SessionManager.getInstance().getToken());
        Log.d("DeckManager", "KryoMessage packaged, sending...");

        new Thread(() -> {
            Log.d("DeckManager", "Sending KryoMessage in new thread...");
            ClientManager.getInstance().getKryoManager().send(kryoMessage);
            Log.d("DeckManager", "KryoMessage sent.");
        }).start();
    }



    public static void infoCard(Context context, Card card) {
        if (card == null || context == null) return;

        EntityType type;
        try {
            type = EntityType.valueOf(card.getName());
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "Carte inconnue", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construire le texte avec toutes les infos
        StringBuilder info = new StringBuilder();
        info.append("Nom : ").append(type.name()).append("\n");
        info.append("Type : ").append(type.getType()).append("\n");

        if (type.getShapeType() != null) {
            info.append("Shape : ").append(type.getShapeType()).append("\n");
        }

        if (type.getWeaponType() != null) {
            info.append("Weapon : ").append(type.getWeaponType()).append("\n");
        }

        info.append("Max Health : ").append(type.getMaxHealth()).append("\n");
        info.append("Armor : ").append(type.getArmor()).append("\n");
        info.append("Passive Heal : ").append(type.getPassiveHeal()).append("\n");
        info.append("Speed : ").append(type.getBase_speed()).append("\n");
        info.append("Freeze Time : ").append(type.getFreeze_time()).append("\n");
        info.append("Create Time : ").append(type.getCreate_time()).append("\n");

        if (type.getCost() != null && !type.getCost().isEmpty()) {
            info.append("Coût :\n");
            for (Map.Entry<ResourcesType, Integer> entry : type.getCost().entrySet()) {
                info.append("  ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }
        }

        if (type.getCategory() != null) {
            info.append("Catégorie : ").append(type.getCategory()).append("\n");
        }

        // Affichage de la popup
        new AlertDialog.Builder(context)
            .setTitle("Infos Carte")
            .setMessage(info.toString())
            .setPositiveButton("Fermer", null)
            .show();
    }

    /**
     * Sélectionne un deck et notifie le serveur.
     * @param deckName le nom du deck à sélectionner
     */
    public static void selectDeck(String deckName) {
        Log.d("DeckManager", "selectDeck called: deckName=" + deckName);

        if (deckName == null || deckName.isEmpty()) {
            Log.d("DeckManager", "selectDeck aborted: invalid deckName");
            return;
        }

        // Vérifie que le deck existe localement
        Deck deck = SessionManager.getInstance().getDecks().get(deckName);
        if (deck == null) {
            Log.d("DeckManager", "selectDeck aborted: deck does not exist locally");
            return;
        }

        // Met à jour le deck courant côté client
        SessionManager.getInstance().setCurrentDeck(deck,deckName);
        Log.d("DeckManager", "Updated currentDeck locally: " + deckName);

        // Prépare les clés pour la requête
        HashMap<String, String> keys = new HashMap<>();
        keys.put("current_deck", deckName);

        // Crée la DeckRequest
        DeckRequest request = RequestFactory.createDeckRequest(DeckRequestType.CHANGE_CURRENT_DECK, keys);
        Log.d("DeckManager", "DeckRequest created: " + request);

        // Packager la requête avec le token
        KryoMessage kryoMessage = KryoMessagePackager.packDeckRequest(request, SessionManager.getInstance().getToken());
        Log.d("DeckManager", "KryoMessage packaged, sending...");

        // Envoie la requête dans un thread séparé
        new Thread(() -> {
            Log.d("DeckManager", "Sending KryoMessage in new thread...");
            ClientManager.getInstance().getKryoManager().send(kryoMessage);
            Log.d("DeckManager", "KryoMessage sent.");
        }).start();
    }







}
