package io.github.server.server_engine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import io.github.server.data.ServerGame;
import io.github.server.data.network.ServerNetwork;
import io.github.shared.data.EnumsTypes.GameModeType;
import io.github.shared.data.EnumsTypes.MapName;
import io.github.shared.data.EnumsTypes.EventType;
import io.github.shared.data.network.ClientNetwork;
import io.github.shared.data.network.Player;

public final class GameMaker {

    /**
     * Crée une partie à partir d'une liste de clients.
     * @param playersForGame liste des clients participant
     * @param mode GameMode
     * @return l'objet Game créé
     */
    public static ServerGame createGame(ArrayList<ClientNetwork> playersForGame, GameModeType mode) {

        // Conversion des ClientNetwork en Player
        ArrayList<Player> playerList = new ArrayList<>();
        ArrayList<Player> teamDefault = new ArrayList<>();
        HashMap<String, ArrayList<Player>> playerTeam = new HashMap<>();

        for (ClientNetwork cn : playersForGame) {
            // Crée un Player à partir du ClientNetwork
            Player p = new Player(cn);
            playerList.add(p);
            teamDefault.add(p);
        }

        playerTeam.put("TEAM_DEFAULT", teamDefault);


        playerTeam.put("TEAM_DEFAULT", teamDefault);

        // Choix d'une map aléatoire pour ce GameMode
        MapName mapName = mode.getAssociated_map().get(new Random().nextInt(mode.getAssociated_map().size()));

        // Événement initial
        EventType eventType = EventType.START;

        // Temps de jeu max pour ce mode
        long timeLeft = mode.getMAX_GAME_TIME();

        // Création de la Game
        ServerGame serverGame = new ServerGame(UUID.randomUUID(), playerTeam, playerList, mode, mapName, eventType, timeLeft);

        // Ajout de la Game dans le HashMap par GameModeType
        ServerNetwork.getInstance().getGameModeArrayListOfGames().computeIfAbsent(mode, k -> new ArrayList<ServerGame>()).add(serverGame);

        System.out.println("Game created: " + playersForGame.size() + " players in mode " + mode);

        return serverGame;
    }

}
