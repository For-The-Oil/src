package io.github.android.gui;

public class Card {
    private final int imageResId;
    private final String name;

    public Card(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public int getImageResId() { return imageResId; }
    public String getName() { return name; }


}
