package io.github.shared.local.data.EnumsTypes;

public enum EntityType {
    Barrack(Type.Building), Factory(Type.Building), Garage(Type.Building),
    INFANTRY(Type.Unit), HEAVE_INFANTRY(Type.Unit), TANK(Type.Unit), MOTORIZED(Type.Unit), WALKER(Type.Unit), AIRCRAFT(Type.Unit), NAVAL(Type.Unit);

    private final Type type;

    EntityType(Type type) {
        this.type = type;
    }

    public Type getType(){
        return type;
    }
    public enum Type{
        Building,Unit
    }
}
