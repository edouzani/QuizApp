package com.almadev.znaniesila.model;

import java.io.Serializable;

/**
 * Created by Aleksey on 24.09.2015.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int points;
    private int Answer;
    private String correct_ans_explanation;
    private String wrong_ans_explanation;
    private String question;
    private int negative_points;
    private int duration_in_seconds;
    private int question_type;

    private QuestionState state = QuestionState.UNDEF;

    public int getPoints() {
        return points;
    }

    public int getAnswer() {
        return Answer;
    }

    public String getCorrect_ans_explanation() {
        return correct_ans_explanation;
    }

    public String getWrong_ans_explanation() {
        return wrong_ans_explanation;
    }

    public String getQuestion() {
        return question;
    }

    public int getNegative_points() {
        return negative_points;
    }

    public int getDuration_in_seconds() {
        return duration_in_seconds;
    }

    public int getQuestion_type() {
        return question_type;
    }

    public QuestionState getState() {
        return state;
    }

    public void setState(QuestionState pState) {
        state = pState;
    }
}
