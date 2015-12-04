package com.almadev.znaniesila.model;

import android.util.Log;

import com.almadev.znaniesila.ZSApp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
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
        if (Questions == null) {
            return new LinkedList<>();
        } else {
            return new LinkedList<>(Questions);
        }
    }

    public List<Question> getSortedQuestions(int amount) {
        if (Questions == null) {
            return new LinkedList<>();
        }

        List<Question> undefQuestions = new LinkedList<>();
        List<Question> viewedQuestions = new LinkedList<>();
        List<Question> correctQuestions = new LinkedList<>();
        List<Question> wrongQuestions = new LinkedList<>();

        for (Question q : Questions) {
            switch (q.getState()) {
                case UNDEF:
                    undefQuestions.add(q);
                    break;
                case CORRECT:
                    correctQuestions.add(q);
                    break;
                case VIEWED:
                    viewedQuestions.add(q);
                    break;
                case WRONG:
                    wrongQuestions.add(q);
                    break;
                default:
                    Log.e("Quiz", "WTF?!?! - " + q.getState());
            }
        }

        List<Question> result = new LinkedList<>();
        result.addAll(shuffleAndGet(undefQuestions, amount));
        if (result.size() < amount) {
            result.addAll(shuffleAndGet(viewedQuestions, amount - result.size()));
        }
        if (result.size() < amount) {
            result.addAll(shuffleAndGet(wrongQuestions, amount - result.size()));
        }
        if (result.size() < amount) {
            result.addAll(shuffleAndGet(correctQuestions, amount - result.size()));
        }
        return result;
    }

    private List<Question> shuffleAndGet(List<Question> questions, int amount) {
        Collections.shuffle(questions);
        return questions.subList(0, amount > questions.size() ? questions.size() : amount);
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
        if (Questions == null) {
            QuizHolder.getInstance(ZSApp.sContext).deleteQuiz(id);
            return i;
        }
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

    public Question getById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (Question q : Questions) {
            if (q.getStory_order_id() != null && q.getStory_order_id().equals(id)) {
                return q;
            }
        }
        return null;
    }
}
