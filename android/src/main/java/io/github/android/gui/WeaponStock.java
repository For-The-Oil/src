package io.github.android.gui;

public class WeaponStock {
    public final String id;
    public final String name;
    public final int iconRes;
    public int quantity;

    public WeaponStock(String id, String name, int iconRes, int quantity) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.quantity = quantity;
    }
}
