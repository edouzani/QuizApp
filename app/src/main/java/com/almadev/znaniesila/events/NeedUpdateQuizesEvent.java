package com.almadev.znaniesila.events;

/**
 * Created by Aleksey on 25.09.2015.
 */
public class NeedUpdateQuizesEvent {
    private String v;

    public NeedUpdateQuizesEvent(String newVersion) {
        v = newVersion;
    }

    public String getVersion() {
        return v;
    }
}
