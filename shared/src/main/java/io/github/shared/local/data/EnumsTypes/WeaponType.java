package io.github.shared.local.data.EnumsTypes;

public enum WeaponType {
    RIFLE(Type.Range), SHOTGUN(Type.Range), BAZOOKA(Type.Range),
    SWORD(Type.Melee), SPEAR(Type.Melee), CHAINSAW(Type.Melee);
    private final Type type;

    WeaponType(Type type) {
        this.type = type;
    }

    public Type getType(){
        return type;
    }
    public enum Type{
        Range,Melee
    }
}
