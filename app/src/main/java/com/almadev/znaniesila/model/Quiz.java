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

    private int record;
    private int answeredQuestions;

    public List<Question> getQuestions() {
        return Questions;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int pRecord) {
        record = pRecord;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(int pAnsweredQuestions) {
        answeredQuestions = pAnsweredQuestions;
    }
}
