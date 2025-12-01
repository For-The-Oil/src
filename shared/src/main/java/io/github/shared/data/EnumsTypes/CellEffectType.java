package io.github.shared.data.enumsTypes;


public enum CellEffectType {
    NONE(1f),
    POISON_DAMAGE(3),
    SLOW(2f),
    SPEED_BOOST(0.5f);

    private final float movingCost;

    CellEffectType(float movingCost) {
        this.movingCost = movingCost;
    }

    public float getMovingCost() {
        return movingCost;
    }
}

