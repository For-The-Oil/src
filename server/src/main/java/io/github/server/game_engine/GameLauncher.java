package io.github.server.game_engine;

import io.github.server.data.Game;

public class GameLauncher extends Thread {

    private final Game game;
    private volatile boolean running = true; // pour contrôler l'arrêt propre

    public GameLauncher(Game game) {
        super("GameThread-" + game.getGAME_UUID());
        this.game = game;
    }

    @Override
    public void run() {
        System.out.println("Game loop started for game: " + game.getGAME_UUID());
        while (running) {
            try {
                // Ici tu peux mettre la logique serveur de la game
                //System.out.println("Game " + game.getGAME_UUID() + " running...");

                // Pause de 5 secondes
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Game loop interrupted for game: " + game.getGAME_UUID());
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game loop stopped for game: " + game.getGAME_UUID());
    }

    /** Méthode pour arrêter proprement la game */
    public void stopGame() {
        running = false;
        //disconnect everyone & redirect them to the main server
        this.interrupt(); // pour sortir d’un sleep éventuel
    }

    public Game getGame() {
        return game;
    }
}
