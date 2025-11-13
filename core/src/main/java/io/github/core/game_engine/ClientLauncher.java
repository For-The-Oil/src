package io.github.core.game_engine;

public class ClientLauncher extends Thread {

    private final ClientGame serverGame;
    public ClientLauncher(ClientGame serverGame) {
        super("GameThread-" + serverGame.getGAME_UUID());
        this.serverGame = serverGame;
        init();
    }
    private void init(){
        //ici start kryo avec this.serverGame.getRequestQueue(); en Paramètres pour récupérer les requests
    }


    @Override
    public void run() {
        System.out.println("Game loop started for game: " + serverGame.getGAME_UUID());
        while (serverGame.isRunning()) {
            float frameTime = getTimeSinceLastFrame();
            //serverGame.setAccumulator(serverGame.getAccumulator() + frameTime);

//            while (serverGame.getAccumulator() >= FIXED_TIME_STEP) {
//                //Mise à jour ECS
//                serverGame.getWorld().setDelta(FIXED_TIME_STEP / 1000f); // converti en secondes pour Artémis
//                serverGame.getWorld().process();
//
//                // Exécuter les instructions en attente
//                while (!serverGame.isEmptyExecutionQueue()) {
//                    Instruction instruction = serverGame.getExecutionQueue().poll();
//                    if(instruction == null)continue;
//                    InstructionManager.executeInstruction(instruction, serverGame);
//                }
//                serverGame.setAccumulator(serverGame.getAccumulator() - FIXED_TIME_STEP);
//            }

//       Envoi des instructions ici
//       Traiter les requests  ici
        }

        System.out.println("Game loop stopped for game: " + serverGame.getGAME_UUID());
    }

    private float getTimeSinceLastFrame() {
        long now = System.currentTimeMillis();
        float delta = now - serverGame.getLastTime();
        serverGame.setLastTime(now);
        return delta;
    }

    public void stopGame() {
        serverGame.stopRunning();
    }

    public ClientGame getGame() {
        return serverGame;
    }

}
