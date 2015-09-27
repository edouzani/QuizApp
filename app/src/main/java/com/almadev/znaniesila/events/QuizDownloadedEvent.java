package com.almadev.znaniesila.events;

/**
 * Created by Aleksey on 25.09.2015.
 */
public class QuizDownloadedEvent {
    private String id;

    public QuizDownloadedEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
