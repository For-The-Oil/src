package io.github.shared.shared_engine.manager;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RobotUUID {

    // Signature fixe : identifie un robot
    private static final long ROBOT_SIGNATURE = 0xABCDL;

    public static UUID generate(int teamId) {
        if (teamId < 0 || teamId > 255) {
            throw new IllegalArgumentException("teamId doit être entre 0 et 255");
        }

        long randomPart = ThreadLocalRandom.current().nextLong() & 0x0000FFFFFFFFFFFFL;

        long mostSigBits =
            (ROBOT_SIGNATURE << 48) | ((long) teamId << 40) | randomPart;

        long leastSigBits = ThreadLocalRandom.current().nextLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public static boolean isRobot(UUID uuid) {
        if(uuid == null)return false;
        long signature = (uuid.getMostSignificantBits() >>> 48) & 0xFFFF;
        return signature == ROBOT_SIGNATURE;
    }

    public static int getTeam(UUID uuid) {
        if (!isRobot(uuid)) {
            throw new IllegalArgumentException("not robot");
        }
        return (int) ((uuid.getMostSignificantBits() >>> 40) & 0xFF);
    }
}
