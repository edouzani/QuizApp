package com.almadev.znaniesila.model;

import java.io.Serializable;
import java.util.LinkedList;
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
//        for (int i = 0; i < Questions.size(); i ++) {
//            Questions.get(i).setLocal_id(i);
//        }
        return new LinkedList<>(Questions);
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
        int i = 0;
        for (Question q : Questions) {
            if (q.getState() == QuestionState.CORRECT) {
                i++;
            }
        }
        return i;
    }

    public void setAnsweredQuestions(int pAnsweredQuestions) {
        answeredQuestions = pAnsweredQuestions;
    }

    public void updateQuestions(List<Question> updates) {
        for (Question q : updates) {
            Questions.get(q.getLocal_id()).setState(q.getState());
        }
    }
}
