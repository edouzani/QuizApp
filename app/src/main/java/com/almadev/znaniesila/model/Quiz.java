package com.almadev.znaniesila.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Aleksey on 25.09.2015.
 */
public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Question> Questions;

    private String id;


    public List<Question> getQuestions() {
        return Questions;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }
}
