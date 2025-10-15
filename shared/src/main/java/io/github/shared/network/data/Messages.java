package com.fortheoil.shared.network.data;

public class Messages {

    public static class PlayerInput {
        public int playerId;
        public int actionType;
        public float targetX;
        public float targetY;
    }

    public static class UnitState {
        public int unitId;
        public float x, y;
        public int hp;
    }
}
