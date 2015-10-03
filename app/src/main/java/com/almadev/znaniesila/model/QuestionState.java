package com.almadev.znaniesila.model;

/**
 * Created by Aleksey on 28.09.2015.
 */
public enum QuestionState {
    CORRECT(1000),
    WRONG(2000),
    VIEWED(3000),
    UNDEF(4000);

    private int weight;

    QuestionState(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
