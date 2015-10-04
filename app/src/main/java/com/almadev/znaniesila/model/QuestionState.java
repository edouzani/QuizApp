package com.almadev.znaniesila.model;

/**
 * Created by Aleksey on 28.09.2015.
 */
public enum QuestionState {
    CORRECT(400000),
    WRONG(300000),
    VIEWED(200000),
    UNDEF(100000);

    private int weight;

    QuestionState(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
