package com.akscardgames.cardgamesproject.general;

import androidx.annotation.NonNull;

public class Card {
    public String value;
    public char suit;
    public int img_res;

    public Card(String value, char suit, int img_res) {
        this.value = value;
        this.suit = suit;
        this.img_res = img_res;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(suit) + String.valueOf(value);
    }
}
